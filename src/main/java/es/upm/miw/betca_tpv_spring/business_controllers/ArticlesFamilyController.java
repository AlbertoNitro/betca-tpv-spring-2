package es.upm.miw.betca_tpv_spring.business_controllers;

import es.upm.miw.betca_tpv_spring.business_services.Barcode;
import es.upm.miw.betca_tpv_spring.documents.*;
import es.upm.miw.betca_tpv_spring.dtos.*;
import es.upm.miw.betca_tpv_spring.exceptions.NotFoundException;
import es.upm.miw.betca_tpv_spring.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Controller
public class ArticlesFamilyController {

    @Autowired
    private FamilyCompositeRepository familyCompositeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticlesFamilyReactRepository articlesFamilyReactRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ArticlesFamilyRepository articlesFamilyRepository;

    @Autowired
    private ArticleReactRepository articleReactRepository;

    @Autowired
    private ProviderReactRepository providerReactRepository;

    private List<String> getSizes() throws IOException {
        String propFileName = "config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        Properties prop = new Properties();
        prop.load(inputStream);
        return Arrays.asList(prop.getProperty("sizes").split(","));
    }

    public List<ArticleFamilyCompleteDto> readFamilyCompositeArticlesList(String description) {
        FamilyComposite familyComplete = familyCompositeRepository.findFirstByDescription(description);
        List<ArticleFamilyCompleteDto> dtos = new ArrayList<>();

        if (familyComplete.getFamilyType() == FamilyType.ARTICLES) {
            for (ArticlesFamily articlesFamily : familyComplete.getArticlesFamilyList()) {
                if (articlesFamily.getFamilyType() == FamilyType.ARTICLES) {
                    dtos.add(new ArticleFamilyCompleteDto(articlesFamily.getFamilyType(), articlesFamily.getDescription(), articlesFamily.getArticlesFamilyList()));
                }
                if (articlesFamily.getFamilyType() == FamilyType.ARTICLE) {
                    Article article = articleRepository.findByCode(articlesFamily.getArticleIdList().get(0));
                    dtos.add(new ArticleFamilyCompleteDto(articlesFamily.getFamilyType(), article.getCode(), article.getDescription(), article.getRetailPrice()));
                }
                if (articlesFamily.getFamilyType() == FamilyType.SIZES) {
                    dtos.add(new ArticleFamilyCompleteDto(articlesFamily.getFamilyType(), articlesFamily.getReference(), articlesFamily.getDescription()));
                }
            }
        } else if (familyComplete.getFamilyType() == FamilyType.SIZES) {
            for (ArticlesFamily articlesFamily : familyComplete.getArticlesFamilyList()) {
                Article article = articleRepository.findByCode(articlesFamily.getArticleIdList().get(0));
                dtos.add(new ArticleFamilyCompleteDto(article.getReference().split("T")[1], article.getStock(), article.getRetailPrice(), article.getCode()));
            }
        }
        return dtos;

    }

    public List<String> readSizes() throws IOException {
        return getSizes();
    }

    public Mono<ArticlesFamilyDto> createArticleFamily(FamilyCompleteDto articlesFamilyDto) throws IOException {
        List<String> sizes = getSizes();
        Flux<Provider> provider =  this.providerReactRepository.findById(articlesFamilyDto.getProvider())
        .switchIfEmpty(Mono.error(new NotFoundException("Provider (" + articlesFamilyDto.getProvider() + ")"))).flux();
        Flux<Article> fluxArticles = provider.flatMap(s-> createArticles(s,articlesFamilyDto,sizes));
        Flux<ArticlesFamily> articlesFamilyFlux = fluxArticles.flatMap(this::createLeaf);
        ArticlesFamily familyCompositeSizesList = new FamilyComposite(FamilyType.SIZES, articlesFamilyDto.getReference(), articlesFamilyDto.getDescription());
        Mono<Void> finalFlux = articlesFamilyFlux.doOnNext(familyCompositeSizesList::add).then();
        return Mono.when(finalFlux).then(articlesFamilyReactRepository.save(familyCompositeSizesList).map(ArticlesFamilyDto::new));
    }

    public Flux<Article> createArticles(Provider provider, FamilyCompleteDto articlesFamilyDto, List<String> sizes){

        int lowerLimit = Integer.parseInt(articlesFamilyDto.getFromSize());
        int upperLimit = Integer.parseInt(articlesFamilyDto.getToSize());
        Flux<Article> articleFlux = Flux.empty();
        int increment = 1;

        if (articlesFamilyDto.getIncrement() > 1 && !articlesFamilyDto.getSizeType())
            increment = articlesFamilyDto.getIncrement();

        for (int index = lowerLimit; index <= upperLimit; index += increment) {

            String size;
            if (articlesFamilyDto.getSizeType())
                size = sizes.get(index);
            else
                size = String.valueOf(index);

            int numCode = index + 1;
            Mono<Article> article = this.articleReactRepository.findFirstByOrderByCodeDesc().switchIfEmpty(Mono.error(new NotFoundException("Article")))
                    .map(s-> Article.builder(new Barcode().generateEan13code(Long.parseLong(s.getCode().substring(0,12))+numCode)).description(articlesFamilyDto.getReference() + " - " + articlesFamilyDto.getDescription() + " T" + size)
                            .reference(articlesFamilyDto.getReference() + " T" + size).provider(provider).build());
            Flux<Article> articleMono  = this.articleReactRepository.saveAll(article.flux());
            articleFlux = articleFlux.mergeWith(articleMono);
        }
        return articleFlux;
    }

    public Mono<ArticlesFamily> createLeaf(Article article)
    {
        return this.articlesFamilyReactRepository.save(new FamilyArticle(article));
    }

    public Flux<ArticlesFamilyCrudDto> readAllArticlesFamily() {
        return this.articlesFamilyReactRepository.findAll()
                .map(ArticlesFamilyCrudDto::new);
    }

    public Mono<ArticlesFamilyCrudDto> searchArticlesFamilyById(String id) {
        return this.articlesFamilyReactRepository.findById(id)
                .map(ArticlesFamilyCrudDto::new);
    }

    public Flux<ArticlesFamilyCrudDto> searchArticlesFamilyByReferenceOrFamilyType(ArticlesFamilySearchDto articlesFamilySearchDto) {
        return this.articlesFamilyReactRepository
                .findByReferenceLikeOrFamilyType(
                        articlesFamilySearchDto.getReference(),
                        articlesFamilySearchDto.getArticleFamily())
                .map(ArticlesFamilyCrudDto::new);
    }

    public Mono<ArticlesFamilyCrudDto> createArticlesFamily(ArticlesFamilyCreationDto articlesFamilyCreationDto) {
        ArticlesFamily articlesFamily;

        if (articlesFamilyCreationDto.getFamilyType() == FamilyType.ARTICLE) {
            articlesFamily = new FamilyArticle(articlesFamilyCreationDto.getArticleObj());

        } else {
            FamilyComposite familyComposite = new FamilyComposite(articlesFamilyCreationDto.getFamilyType(),
                    articlesFamilyCreationDto.getReference(),
                    articlesFamilyCreationDto.getDescription());
            if (articlesFamilyCreationDto.getArticlesFamilyListId() != null && articlesFamilyCreationDto.getArticlesFamilyListId().length > 0) {
                for (int i = 0; i < articlesFamilyCreationDto.getArticlesFamilyListId().length; i++) {
                    String articleFamilyId = articlesFamilyCreationDto.getArticlesFamilyListId()[i];
                    this.articlesFamilyReactRepository.findById(articleFamilyId)
                            .switchIfEmpty(Mono.error(new NotFoundException("ArticleFamily (" + articleFamilyId + ")")))
                            .doOnNext(familyComposite::add).then();
                }
            }
            articlesFamily = familyComposite;
        }

        return this.articlesFamilyReactRepository.save(articlesFamily)
                .map(ArticlesFamilyCrudDto::new);

    }

    public Mono<ArticlesFamilyCrudDto> updateArticlesFamily(String id, ArticlesFamilyCreationDto articlesFamilyCreationDto) {
        Mono<ArticlesFamily> articlesFamily = this.articlesFamilyReactRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("ArticlesFamily id " + articlesFamilyCreationDto.getId())))
                .map(articlesFamily1 -> {
                    articlesFamily1.setFamilyType(articlesFamilyCreationDto.getFamilyType());
                    articlesFamily1.setReference(articlesFamilyCreationDto.getReference());
                    if(articlesFamilyCreationDto.getDescription() != null)articlesFamily1.setDescription(articlesFamilyCreationDto.getDescription());
                    if (articlesFamilyCreationDto.getFamilyType() != FamilyType.ARTICLE) {
                        if (articlesFamilyCreationDto.getArticlesFamilyListId() != null && articlesFamilyCreationDto.getArticlesFamilyListId().length > 0) {
                            articlesFamily1.clearArticleFamilyList();

                            for (int i = 0; i < articlesFamilyCreationDto.getArticlesFamilyListId().length; i++) {
                                String articleFamilyId = articlesFamilyCreationDto.getArticlesFamilyListId()[i];
                                if (this.articlesFamilyRepository.findById(articleFamilyId).isPresent()) {
                                    articlesFamily1.add(this.articlesFamilyRepository.findById(articleFamilyId).get());
                                }
                            }
                        }
                    } else {
                        if (this.articleRepository.findById(articlesFamilyCreationDto.getArticle()).isPresent()) {
                            articlesFamily1.setArticle(this.articleRepository.findById(articlesFamilyCreationDto.getArticle()).get());
                        }

                    }
                    return articlesFamily1;
                });
        return Mono
                .when(articlesFamily)
                .then(this.articlesFamilyReactRepository.saveAll(articlesFamily).next().map(ArticlesFamilyCrudDto::new));
    }

    public Mono<Void> deleteArticlesFamily(String articleFamilyId){
        Mono<ArticlesFamily> articlesFamily = this.articlesFamilyReactRepository.findById(articleFamilyId);
        return Mono
                .when(articlesFamily)
                .then(this.articlesFamilyReactRepository.deleteById(articleFamilyId));
    }
}
