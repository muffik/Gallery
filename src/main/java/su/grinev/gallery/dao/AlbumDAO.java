package su.grinev.gallery.dao;

import org.springframework.beans.factory.annotation.Autowired;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.Album;
import org.springframework.stereotype.Component;
import su.grinev.gallery.model.Image;

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
        /* todo: Zhdankin: есть идеи как предусмотреть конкурентный доступ? т.е. чтобы не было плохих последствий при одновременном выполнении создания альбома двумя пользователями? */
        albums = new HashMap<>();

        this.add("Landscape");
        this.add("Macro");
        this.add("Street");
        this.add("Test");
        this.add("Portrait");
    }

    public List<Album> index(){
        /* todo: Zhdankin: зачем нужно оборачивание в ArrayList? */
        List<Album> albumsList=new ArrayList<>(albums.values());
        return albumsList;
    }

    public Album add(String name){
        Album album=new Album(++ALBUM_COUNT, name);
        albums.put(ALBUM_COUNT, album);
        return album;
    }

    public void remove(int albumId) throws IOException {
        /* todo: Zhdankin: если у нас руками удалили файл из директории альбома, то теперь мы не сможем удалить альбом? */
        if (albums.get(albumId)==null) throw new ResourceNotFoundException("Invalid albumId");
        List<Image> imagesToRemove=imagedao.list(albumId);
        for (Image image: imagesToRemove) {
            File f=new File(imagedao.uploadDirectory+"/"+image.getFileName());
            if (!f.delete()) throw new IOException();
        }
        albums.remove(albumId);
    }

    public Album get(int albumId){
        Album album=albums.get(albumId);
        if (album==null) throw new ResourceNotFoundException("Invalid albumId");
        return album;
    }
}
