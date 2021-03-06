package wp.controller;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wp.bean.ResponseBean;
import wp.service.DDNSService;
import wp.service.ImageStorageService;
import wp.service.NoteService;
import wp.service.UserService;
import wp.util.JWTUtil;
import wp.wrap.AddWrap;
import wp.wrap.UserWrap;


// FIXME title & category即使在不同目录也都必须命名唯一，由于通过名称查询（目前名称重复直接导致提交失败）
// TODO token过期时间自定义，备份sql及images的路径自定义

@RestController
public class NoteController {

    private final static Logger logger = LoggerFactory.getLogger(NoteController.class);

    private UserService userService;

    private NoteService noteService;

    private ImageStorageService imageStorageService;

    private DDNSService ddnsService;

    @Autowired
    public NoteController(UserService userService, NoteService noteService, ImageStorageService imageStorageService,DDNSService ddnsService) {
        this.userService = userService;
        this.noteService = noteService;
        this.imageStorageService = imageStorageService;
        this.ddnsService = ddnsService;
    }


    @PostMapping("token")
    public ResponseBean login(@RequestBody UserWrap userWrap){
        logger.debug("step into");
        return userService.authenticateUser(userWrap.getUserName(), userWrap.getUserPassword());
    }

    @GetMapping("{user}/notes")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean list(@PathVariable String user,@RequestParam("searchInfo") String searchInfo) {
        logger.debug("step into");
        return noteService.getNoteList(user,searchInfo);
    }

    @GetMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean getNote(@PathVariable("id") Integer id, @PathVariable String user) {
        logger.debug("step into");
        return noteService.getNoteById(id,user);
    }

    @DeleteMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean deleteNote(@PathVariable("id") Integer id, @PathVariable String user) {
        logger.debug("step into");
        return noteService.deleteNoteById(id,user);
    }

    @PutMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean updateNote(@PathVariable("id") Integer id, @PathVariable String user, @RequestBody AddWrap addWrap) {
        logger.debug("step into");
        return noteService.updateNoteById(id, addWrap,user);
    }

    @PostMapping("{user}/notes")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean add(@RequestBody AddWrap addWrap, @PathVariable String user) {
        logger.debug("step into");
        return noteService.addNote(addWrap,user);
    }

    @PostMapping("{user}/image")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean imageUpload(@RequestParam("image") MultipartFile file, @PathVariable String user) {
        logger.debug("step into");
        return imageStorageService.store(file);
    }

    @GetMapping("ddns")
    public ResponseBean getDDNSStatus() {
        return ddnsService.getDDNSStatus();
    }

    @GetMapping("{user}/backupfile")
    @ResponseBody
    public ResponseEntity<Resource> getBackUpFile(@PathVariable String user, @RequestParam("token") String token) {
        String userName = JWTUtil.getUsername(token);
        if (!"wp".equals(user) || !user.equals(userName)) {
            throw new UnauthorizedException();
        }
        Resource file = imageStorageService.loadBackupFile();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wp-image-sql.tar.gz\"").body(file);
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBean unauthorized() {
        logger.debug("step into");
        return new ResponseBean(401, "Unauthorized401", null);
    }
}
