package su.grinev.gallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.grinev.gallery.dao.AlbumDAO;
import su.grinev.gallery.dao.ImageDAO;
import su.grinev.gallery.exception.DataFormatException;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.Image;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
public class FileController {

    private final AlbumDAO albumDAO;
    private final ImageDAO imageDAO;

    @Value("${uploadDir}")
    public String uploadDirectory;

    @Autowired
    public FileController(AlbumDAO albumDAO, ImageDAO imageDAO){
        this.albumDAO=albumDAO;
        this.imageDAO=imageDAO;
    }

    @RequestMapping(value="/image/upload", method=RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    void provideUploadInfo() {
        throw new DataFormatException("Use POST for file uploading.");
    }

    @RequestMapping(value="/image/upload", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Image uploadImage(@RequestPart("albumId") String albumId,
                                           @RequestPart(value = "displayName", required = false) String displayName,
                                           @RequestPart("fileName") String fileName,
                                           @RequestPart("file") MultipartFile file){
        if (albumDAO.get(Integer.parseInt(albumId))==null) throw new ResourceNotFoundException("Album doesn't exist!");
        if (displayName==null) displayName=fileName;
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(uploadDirectory+"\\"+ albumId+"\\"+ fileName)));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                throw new DataFormatException("File upload error!");
            }
        } else {
            throw new DataFormatException("File must not be empty!");
        }
        File f = new File(uploadDirectory+"\\"+ albumId+"\\"+ fileName);
        String mimetype= new MimetypesFileTypeMap().getContentType(f);
        String type = mimetype.split("/")[0];
        if(!type.equals("image")) {
        //    System.out.println("It's not an image!");
            f.delete();
            throw new DataFormatException("It's not an image!");
        }
        return imageDAO.add(Integer.parseInt(albumId), displayName, fileName);
    }

    @RequestMapping(value="/image", method=RequestMethod.GET, produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImage(@RequestParam("imageId") int imageId, HttpServletResponse response){
        if (imageDAO.get(imageId)==null) throw new ResourceNotFoundException("Image doesn't exists!");
        int read=0;
        InputStream inputStream;
        OutputStream outputStream;
        try {
            byte[] bytes = new byte[65536];
            inputStream = new FileInputStream(uploadDirectory+"\\"+Integer.toString(imageDAO.get(imageId).getAlbumId())+ "\\" + imageDAO.get(imageId).getFileName());
            outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename="+imageDAO.get(imageId).getFileName());
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            throw new DataFormatException("File not found!");
        } catch (IOException e) {
            throw new DataFormatException("File download error!");
        }
    }

}
