package com.android.projectandroid.data.productModel;

import com.android.projectandroid.data.userModel.NormalUser;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ProductDeserializer implements JsonDeserializer<Product> {
    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Product product = new Product();

        // Handle basic fields
        if (jsonObject.has("_id") && !jsonObject.get("_id").isJsonNull()) {
            product.setId(jsonObject.get("_id").getAsString());
        }

        if (jsonObject.has("productName") && !jsonObject.get("productName").isJsonNull()) {
            product.setProductName(jsonObject.get("productName").getAsString());
        }

        if (jsonObject.has("images") && !jsonObject.get("images").isJsonNull()) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> images = context.deserialize(jsonObject.get("images"), listType);
            product.setImages(images);
        }

        if (jsonObject.has("category") && !jsonObject.get("category").isJsonNull()) {
            product.setCategory(jsonObject.get("category").getAsString());
        }

        if (jsonObject.has("originalPrice") && !jsonObject.get("originalPrice").isJsonNull()) {
            product.setOriginalPrice(jsonObject.get("originalPrice").getAsDouble());
        }

        if (jsonObject.has("condition") && !jsonObject.get("condition").isJsonNull()) {
            product.setCondition(jsonObject.get("condition").getAsString());
        }

        if (jsonObject.has("purchasePrice") && !jsonObject.get("purchasePrice").isJsonNull()) {
            product.setPurchasePrice(jsonObject.get("purchasePrice").getAsDouble());
        }

        if (jsonObject.has("sellPrice") && !jsonObject.get("sellPrice").isJsonNull()) {
            product.setPurchasePrice(jsonObject.get("sellPrice").getAsDouble());
        }

        if (jsonObject.has("shortDescription") && !jsonObject.get("shortDescription").isJsonNull()) {
            product.setShortDescription(jsonObject.get("shortDescription").getAsString());
        }

        if (jsonObject.has("detailedDescription") && !jsonObject.get("detailedDescription").isJsonNull()) {
            product.setDetailedDescription(jsonObject.get("detailedDescription").getAsString());
        }

        // Handle userId - can be string or object
        if (jsonObject.has("userId") && !jsonObject.get("userId").isJsonNull()) {
            JsonElement userIdElement = jsonObject.get("userId");
            if (userIdElement.isJsonPrimitive()) {
                // userId is a string
                product.setUserId(userIdElement.getAsString());
            } else if (userIdElement.isJsonObject()) {
                // userId is a populated user object
                JsonObject userObject = userIdElement.getAsJsonObject();
                NormalUser user = context.deserialize(userObject, NormalUser.class);
                product.setUser(user);
                if (userObject.has("_id")) {
                    product.setUserId(userObject.get("_id").getAsString());
                }
            }
        }

        if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
            product.setStatus(jsonObject.get("status").getAsString());
        }

        if (jsonObject.has("createdAt") && !jsonObject.get("createdAt").isJsonNull()) {
            product.setCreatedAt(jsonObject.get("createdAt").getAsString());
        }

        if (jsonObject.has("updatedAt") && !jsonObject.get("updatedAt").isJsonNull()) {
            product.setUpdatedAt(jsonObject.get("updatedAt").getAsString());
        }

        if (jsonObject.has("rejectionReason") && !jsonObject.get("rejectionReason").isJsonNull()) {
            product.setRejectionReason(jsonObject.get("rejectionReason").getAsString());
        }

        return product;
    }
}