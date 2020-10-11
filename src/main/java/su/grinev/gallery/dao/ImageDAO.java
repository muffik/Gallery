package su.grinev.gallery.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.Image;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImageDAO {
    private static int IMAGE_COUNT;
    @Value("${uploadDir}")
    public String uploadDirectory;
    private Map<Integer,Image> images;

    {
        images=new HashMap<>();
    }

    public List<Image> list(int albumId){
        List<Image> albumImages=new ArrayList<>();
        for (Map.Entry<Integer, Image> image: images.entrySet()) {
            if (image.getValue().getAlbumId() == albumId) albumImages.add(image.getValue());
        }
        return albumImages;
    }

    public Image add(int albumId, String originalName, String fileName, String displayName){
        Image image=new Image(++IMAGE_COUNT, albumId, originalName, displayName, fileName);
        image.setUploadDate(LocalDate.now());
        images.put(IMAGE_COUNT, image);
        return image;
    }

    public void remove(int imageId) throws IOException {
        Image image=images.get(imageId);
        if (image==null) throw new ResourceNotFoundException("Invalid imageId");
        File imageFile=new File(uploadDirectory+"/"+images.get(imageId).getFileName());
        imageFile.delete();
        if (imageFile.exists()) throw new IOException();
        images.remove(imageId);
    }

    public Image get(int imageId){
        Image image=images.get(imageId);
        if (image==null) throw new ResourceNotFoundException("Invalid imageId");
        return image;
    }

}

