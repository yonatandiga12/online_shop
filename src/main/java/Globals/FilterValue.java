package Globals;

public interface FilterValue {
    boolean filter(); //TODO change to receive catalogItem or something
}

class PriceRange implements FilterValue{
    private int lowerBound;
    private int upperBound;
    public PriceRange(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean filter() {
        return false;
    }
}

class ItemRating implements FilterValue{
    private int lowerBound;
    private int upperBound;
    public ItemRating(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean filter() {
        return false;
    }
}

class Category implements FilterValue{
    private int lowerBound;
    private int upperBound;
    public Category(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean filter() {
        return false;
    }
}

class StoreRating implements FilterValue{
    private int rating;
    public StoreRating(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean filter() {
        return false;
    }
}

class InStock implements FilterValue{
    private boolean inStock;
    public InStock(boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public boolean filter() {
        return inStock;
    }
}