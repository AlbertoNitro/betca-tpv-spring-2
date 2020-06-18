package es.upm.miw.betca_tpv_spring.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import es.upm.miw.betca_tpv_spring.documents.Article;
import es.upm.miw.betca_tpv_spring.documents.ArticlesFamily;
import es.upm.miw.betca_tpv_spring.documents.FamilyType;

import java.util.List;

public class ArticlesFamilyCreationDto {

    @JsonInclude(Include.NON_NULL)
    private String id;

    private FamilyType familyType;

    private String reference;

    private String description;

    @JsonInclude(Include.NON_NULL)
    private String[] articlesFamilyListId;

    @JsonInclude(Include.NON_NULL)
    private String article;

    private Article articleObj;

    public ArticlesFamilyCreationDto() {
        // Empty for framework
    }


    public ArticlesFamilyCreationDto(FamilyType familyType, String reference, String description,  String[] articlesFamilyListId, String article) {
        this.familyType = familyType;
        this.reference = reference;
        this.description = description;
        this.articlesFamilyListId = articlesFamilyListId;
        this.article = article;
    }

    public ArticlesFamilyCreationDto(FamilyType familyType, String reference, String description,  String[] articlesFamilyListId, Article articleObj) {
        this.familyType = familyType;
        this.reference = reference;
        this.description = description;
        this.articlesFamilyListId = articlesFamilyListId;
        this.articleObj = articleObj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FamilyType getFamilyType() {
        return familyType;
    }

    public void setFamilyType(FamilyType familyType) {
        this.familyType = familyType;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArticle(){
        return article;
    }

    public void setArticle(String article){
        this.article = article;
    }

    public Article getArticleObj(){
        return articleObj;
    }

    public void setArticleObj(Article article){
        this.articleObj = article;
    }

    public String[] getArticlesFamilyListId() {
        return articlesFamilyListId;
    }

    public void setArticlesFamilyListId(String[] articlesFamilyListId) {
        this.articlesFamilyListId = articlesFamilyListId;
    }

    @Override
    public String toString() {
        return "ArticlesFamilyDto{" +
                ", familyType=" + familyType +
                ", reference='" + reference + '\'' +
                '}';
    }


}
