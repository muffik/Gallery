package su.grinev.gallery.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.Album;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AlbumDAO {
    private static int ALBUM_COUNT;
    private Map<Integer,Album> albums;

    private final ImageDAO imagedao;

    @Autowired
    public AlbumDAO(ImageDAO imagedao) {
        this.imagedao=imagedao;
        albums = new HashMap<>();

        this.add("Landscape");
        this.add("Macro");
        this.add("Street");
        this.add("Test");
        this.add("Portrait");
    }

    public List<Album> index(){
        List<Album> albumsList=new ArrayList<>(albums.values());
        return albumsList;
    }

    public Album add(String name){
        Album album=new Album(++ALBUM_COUNT, name);
        File directory = new File(imagedao.uploadDirectory+"\\"+Integer.toString(album.getId()));
        if (! directory.exists()) directory.mkdir();
        if (directory.exists()) {
            albums.put(album.getId(), album);
        } else throw new ResourceNotFoundException();
        return album;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void remove(int albumId) throws IOException {
        if (albums.get(albumId)==null) throw new ResourceNotFoundException("Invalid albumId");
        File dir = new File(imagedao.uploadDirectory+"\\"+albumId);
        if (deleteDirectory(dir)) {
            albums.remove(albumId);
        } else throw new IOException();
    }

    public Album get(int albumId){
        Album album=albums.get(albumId);
        if (album==null) throw new ResourceNotFoundException("Invalid albumId");
        return album;
    }
}
