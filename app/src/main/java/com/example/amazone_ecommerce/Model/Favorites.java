package com.example.amazone_ecommerce.Model;

public class Favorites {
    private String ProductId,ProductName,ProductPrice,ProductMenuId,ProductImage,ProductDiscount,ProductDescription,UserPhone;

    public Favorites() {
    }

    public Favorites(String productId, String productName, String productPrice, String productMenuId, String productImage, String productDiscount, String productDescription, String userPhone) {
        ProductId = productId;
        ProductName = productName;
        ProductPrice = productPrice;
        ProductMenuId = productMenuId;
        ProductImage = productImage;
        ProductDiscount = productDiscount;
        ProductDescription = productDescription;
        UserPhone = userPhone;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(String productPrice) {
        ProductPrice = productPrice;
    }

    public String getProductMenuId() {
        return ProductMenuId;
    }

    public void setProductMenuId(String productMenuId) {
        ProductMenuId = productMenuId;
    }

    public String getProductImage() {
        return ProductImage;
    }

    public void setProductImage(String productImage) {
        ProductImage = productImage;
    }

    public String getProductDiscount() {
        return ProductDiscount;
    }

    public void setProductDiscount(String productDiscount) {
        ProductDiscount = productDiscount;
    }

    public String getProductDescription() {
        return ProductDescription;
    }

    public void setProductDescription(String productDescription) {
        ProductDescription = productDescription;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }
}