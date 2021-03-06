package su.grinev.gallery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Image {
    private int id;
    private int albumId;
    private String fileName;
    private String originalName;
    private String displayName;
    private LocalDate uploadDate;

    public Image(int id, int AlbumId, String originalName, String displayName, String fileName){
        this.id=id;
        this.albumId=AlbumId;
        this.originalName=originalName;
        this.displayName=displayName;
        this.fileName=fileName;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    @JsonProperty(value = "fileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @Override
    public String toString(){
        return "Image {" +
                "id=" + this.id +
                ", originalName='" + this.originalName + '\''+
                ", fileName='" + this.fileName + '\''+
                ", displayName='" + this.displayName + '\''+
                ", uploadDate='"+ this.uploadDate.toString() +'\''+'}';
    }
}
