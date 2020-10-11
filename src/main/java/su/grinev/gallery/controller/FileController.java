package su.grinev.gallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;

@Controller
public class FileController {

    private final AlbumDAO albumDAO;
    private final ImageDAO imageDAO;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${uploadDir}")
    public String uploadDirectory;

    @Autowired
    public FileController(AlbumDAO albumDAO, ImageDAO imageDAO){
        this.albumDAO=albumDAO;
        this.imageDAO=imageDAO;
    }

    public String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest()).toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        return sha1;
    }

    @RequestMapping(value="/image/upload", method=RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody void provideUploadInfo() {
        throw new DataFormatException("Use POST for file uploading.");
    }

    @RequestMapping(value="/image/upload", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Image uploadImage(@RequestPart(value = "albumId") String albumId,
                                           @RequestPart(value = "displayName", required = false) String displayName,
                                           @RequestPart(value = "file") MultipartFile file) throws IOException {
        String originalName=file.getOriginalFilename();
        if (displayName==null) displayName=originalName;
        String hashedFilename=this.sha1(displayName+ LocalTime.now());
        String fileName=uploadDirectory+"/"+hashedFilename;
        if (albumDAO.get(Integer.parseInt(albumId))==null) throw new ResourceNotFoundException("Album doesn't exist!");

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                File f=new File(fileName);
                if(!f.exists()){ f.createNewFile(); }
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                throw new DataFormatException("File upload error!");
            }
        } else {
            throw new DataFormatException("File must not be empty!");
        }
        return imageDAO.add(Integer.parseInt(albumId), originalName, hashedFilename, displayName);
    }

    @RequestMapping(value="/image", method=RequestMethod.GET, produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImage(@RequestParam("imageId") int imageId, HttpServletResponse response){
        if (imageDAO.get(imageId)==null) throw new ResourceNotFoundException("Image doesn't exists!");
        InputStream inputStream;
        OutputStream outputStream;
        String fileName=uploadDirectory+"/"+imageDAO.get(imageId).getFileName();
        try {
            byte[] bytes = new byte[65536];
            inputStream = new FileInputStream(fileName);
            outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename="+imageDAO.get(imageId).getOriginalName());
            while ((inputStream.read(bytes)) != -1) {
                outputStream.write(bytes);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException("File not found!");
        } catch (IOException e) {
            throw new ResourceNotFoundException("File download error!");
        }
    }

}
