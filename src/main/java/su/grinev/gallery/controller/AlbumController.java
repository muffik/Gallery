package su.grinev.gallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import su.grinev.gallery.dao.AlbumDAO;
import org.springframework.beans.factory.annotation.Autowired;
import su.grinev.gallery.dao.ImageDAO;
import su.grinev.gallery.exception.ResourceNotFoundException;
import su.grinev.gallery.model.Album;
import su.grinev.gallery.model.Image;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
class AlbumController {
    /* todo: Zhdankin: не используемые поля затрудняют читаемость, их лучше сразу удалять */
    private static final String DEFAULT_PAGE_NUM="0";
    private static final String DEFAULT_PAGE_SIZE="100";

    private final AlbumDAO albumDAO;
    private final ImageDAO imageDAO;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public AlbumController(AlbumDAO albumDAO, ImageDAO imageDAO){
        this.albumDAO=albumDAO; this.imageDAO=imageDAO;
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    /* todo: Zhdankin: использовать Pageable для потранички - ну так себе. Давай лучше без постранички? мешает немного. */
    public @ResponseBody Page<Album> index(@PageableDefault(size = 50, page = 0, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable,
                             HttpServletRequest request, HttpServletResponse response){
        Page<Album> page= new PageImpl(albumDAO.index(), pageable, albumDAO.index().size());
        return page;
    }

    @RequestMapping(value = "/album/list", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Page<Image> index(@RequestParam(value = "id", required = true) int albumId,
                                            @PageableDefault(size = 50, page = 0, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable,
                                           HttpServletRequest request, HttpServletResponse response){
        Page<Image> page= new PageImpl(imageDAO.list(albumId), pageable, imageDAO.list(albumId).size());
        return page;
    }

    @RequestMapping(value = "/album", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody Album show(@RequestParam(value = "id", required = false) Integer id){
        if (albumDAO.get(id)!=null) return albumDAO.get(id);
        throw new ResourceNotFoundException();
    }

    @RequestMapping(value ="/album", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    /* todo: Zhdankin: давай лучше сделаем так чтобы на вход создания альбома приходил json-объект, а не его поля в строке запроса? */
    public @ResponseBody Album add(@RequestParam(value = "name", required = true) String name){
        return albumDAO.add(name);
    }

    @RequestMapping(value = "/album", method = RequestMethod.DELETE, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody void removeAlbum(@RequestParam(value = "albumId", required = false) Integer albumId) throws IOException {
        albumDAO.remove(albumId);
    }

    @RequestMapping(value = "/image", method = RequestMethod.DELETE, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody void removeImage(@RequestParam(value = "imageId", required = false) Integer imageId) throws IOException {
        imageDAO.remove(imageId);
    }

}
