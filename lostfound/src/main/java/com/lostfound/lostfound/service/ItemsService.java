package com.lostfound.lostfound.service;

import com.lostfound.lostfound.Model.Items;
import com.lostfound.lostfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemsService {

    @Autowired
    private ItemRepository itemsRepository;

    // Add item (auto-link user ID and timestamp)
    public Items addItem(Items item, String userid) {
        item.setUserid(userid);
        item.init(); // auto-generate id and createdAt
        return itemsRepository.save(item);
    }

    // Get all items for a user
    public List<Items> getAllItemsForUser(String userid) {
        return itemsRepository.findByUserid(userid);
    }

    // Get items for a user by type (LOST / FOUND)
    public List<Items> getItemsByType(String userid, String type) {
        return itemsRepository.findByUseridAndType(userid, type.toUpperCase());
    }

    // Optional: update item (only if user owns it)
    public Items updateItem(String itemId, Items updatedItem, String userid) {
        Items existing = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existing.getUserid().equals(userid)) {
            throw new RuntimeException("Unauthorized");
        }
        updatedItem.setId(existing.getId());
        updatedItem.setUserid(userid);
        updatedItem.setCreatedAt(existing.getCreatedAt());
        return itemsRepository.save(updatedItem);
    }

    // Optional: delete item (only if user owns it)
    public void deleteItem(String itemId, String userid) {
        Items existing = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existing.getUserid().equals(userid)) {
            throw new RuntimeException("Unauthorized");
        }
        itemsRepository.delete(existing);
    }
}
