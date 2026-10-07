package inventory;

import item.Item;

public class Inventory {
    public static final int BASE_SIZE = 20;
    public static final int ABSOLUTE_MAX = 40;

    private final Item[] slots = new Item[ABSOLUTE_MAX];
    private int maxSize = BASE_SIZE;

    public int getMaxSize() {
        return maxSize;
    }

    public int getSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = Math.min(ABSOLUTE_MAX, Math.max(BASE_SIZE, maxSize));
    }

    public void expand(int slots) {
        setMaxSize(maxSize + slots);
    }

    public Item get(int index) {
        if (index < 0 || index >= maxSize) {
            return null;
        }
        return slots[index];
    }

    public boolean add(Item item) {
        for (int i = 0; i < maxSize; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        return false;
    }

    public Item remove(int index) {
        if (index < 0 || index >= maxSize) {
            return null;
        }
        Item item = slots[index];
        slots[index] = null;
        return item;
    }

    public int countItems() {
        int count = 0;
        for (int i = 0; i < maxSize; i++) {
            if (slots[i] != null) {
                count++;
            }
        }
        return count;
    }

    public boolean isFull() {
        return countItems() >= maxSize;
    }

    public void clear() {
        for (int i = 0; i < maxSize; i++) {
            slots[i] = null;
        }
    }

    public void set(int index, Item item) {
        if (index >= 0 && index < maxSize) {
            slots[index] = item;
        }
    }
}
