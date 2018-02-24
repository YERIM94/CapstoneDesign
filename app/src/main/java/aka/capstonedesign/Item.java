package aka.capstonedesign;

/**
 * Created by 안도희 on 2017-11-30.
 */

public class Item {
    int image;
    String title;
    String imf;

    int getImage() {
        return this.image;
    }
    String getTitle() {
        return this.title;
    }
    String getImf() {
        return this.imf;
    }

    Item(int image, String title, String imf) {
        this.image = image;
        this.title = title;
        this.imf = imf;

    }
}