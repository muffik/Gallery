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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;

@Controller
public class FileController {

    private final AlbumDAO albumDAO;
    private final ImageDAO imageDAO;

    /* todo: Zhdankin: почему protected? */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${uploadDir}")
    public String uploadDirectory;

    @Autowired
    public FileController(AlbumDAO albumDAO, ImageDAO imageDAO){
        /* todo: Zhdankin: попробуй следовать гугл-стайлу, это но не критично в рамках тестовго задания, но при профессиональной разработке нужно его соблюдать */
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

    /* todo: Zhdankin: зачем делать метод, который говорит что не его надо использовать? */
    @RequestMapping(value="/image/upload", method=RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody void provideUploadInfo() {
        throw new DataFormatException("Use POST for file uploading.");
    }

    @RequestMapping(value="/image/upload", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Image uploadImage(@RequestPart(value = "albumId") String albumId,
                                           @RequestPart(value = "displayName", required = false) String displayName,
                                           @RequestPart(value = "file") MultipartFile file) throws IOException {
        /* todo: Zhdankin: логика сохранения файла не должна размещаться в контроллере - ей самое место в Dao */
        String originalName=file.getOriginalFilename();
        if (displayName==null) displayName=originalName;
        /* todo: Zhdankin: смысла в хешировании не особо много, а вот сохранение замедляет */
        String hashedFilename=this.sha1(displayName+ LocalTime.now());
        String fileName=uploadDirectory+"/"+hashedFilename;
        if (albumDAO.get(Integer.parseInt(albumId))==null) throw new ResourceNotFoundException("Album doesn't exist!");

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                File f=new File(fileName);
                /* todo: Zhdankin: какова верятность что такой файл существует? ) */
                if(!f.exists()){ f.createNewFile(); }
                /* todo: Zhdankin: зачем оборачивать в BufferedOutputStream? */
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
                stream.write(bytes);
                /* todo: Zhdankin: лучше использовать try-close синтаксис */
                stream.close();
            } catch (Exception e) {
                throw new DataFormatException("File upload error!");
            }
        } else {
            throw new DataFormatException("File must not be empty!");
        }
        try {
            File f=new File(fileName);
            BufferedImage image=ImageIO.read(f);
            if (image==null){
                if (!f.delete()){
                    throw new IOException();
                }
            //    System.out.println("The file isn't an image!");
                throw new DataFormatException("The file isn't an image!");
            }
        } catch (IOException ex) {
            log.error("Unknown IO exception has occurred!");
        }
        /* todo: Zhdankin: почему бы сразу не объявить параметр int вместо постоянного parseInt? */
        return imageDAO.add(Integer.parseInt(albumId), originalName, hashedFilename, displayName);
    }

    @RequestMapping(value="/image", method=RequestMethod.GET, produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImage(@RequestParam("imageId") int imageId, HttpServletResponse response){
        if (imageDAO.get(imageId)==null) throw new ResourceNotFoundException("Image doesn't exists!");
        InputStream inputStream;
        OutputStream outputStream;
        String fileName=uploadDirectory+"/"+imageDAO.get(imageId).getFileName();
        try {
            /* todo: Zhdankin: лучше использовать try-close синтаксис, так же common-io библиотеку (IOUtils.copy(from, to)) */
            byte[] bytes = new byte[65536];
            inputStream = new FileInputStream(fileName);
            outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename="+imageDAO.get(imageId).getOriginalName());
            while ((inputStream.read(bytes)) != -1) {
                /* todo: Zhdankin: есть подозрение что это портит контент файла, не совсем корректное копирование; если загрузить файл, а потом скачать - размер будет одинаковый? */
                outputStream.write(bytes);
            }
            /* todo: Zhdankin: обязательно ли делать flush? */
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
