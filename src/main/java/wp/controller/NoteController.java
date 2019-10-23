package wp.controller;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wp.bean.ResponseBean;
import wp.service.ImageStorageService;
import wp.service.NoteService;
import wp.service.UserService;
import wp.wrap.AddWrap;
import wp.wrap.UserWrap;


// FIXME title & category都必须命名唯一，由于通过名称查询（目前名称重复直接导致提交失败）
// TODO logout function
// FIXME 每次请求都要查询user,改为缓存（为null查询，否则不查），另外涉及缓存有效期，模仿或使用redis???
@RestController
public class NoteController {
    private static Logger logger = LoggerFactory.getLogger(NoteController.class);

    private UserService userService;

    private NoteService noteService;

    private ImageStorageService imageStorageService;

    @Autowired
    public NoteController(UserService userService, NoteService noteService, ImageStorageService imageStorageService) {
        this.userService = userService;
        this.noteService = noteService;
        this.imageStorageService = imageStorageService;
    }


    @PostMapping("/token")
    public ResponseBean login(@RequestBody UserWrap userWrap){
        return userService.authenticateUser(userWrap.getUserName(), userWrap.getUserPassword());
    }

    @GetMapping("{user}/notes")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean list(@PathVariable String user) {
        return noteService.getNoteList();
    }

    @GetMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean getNote(@PathVariable("id") Integer id, @PathVariable String user) {
        return noteService.getNoteById(id);
    }

    @DeleteMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean deleteNote(@PathVariable("id") Integer id, @PathVariable String user) {
        return noteService.deleteNoteById(id);
    }

    @PutMapping("{user}/notes/{id}")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    // 更新操作，通过id更新title,content,category
    public ResponseBean updateNote(@PathVariable("id") Integer id, @PathVariable String user, @RequestBody AddWrap addWrap) {
        return noteService.updateNoteById(id, addWrap);
    }

    @PostMapping("{user}/notes")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean add(@RequestBody AddWrap addWrap, @PathVariable String user) {
        return noteService.addNote(addWrap);
    }

    @PostMapping("{user}/image")
    @RequiresAuthentication
    @RequiresRoles("admin")
    @RequiresPermissions("all")
    public ResponseBean imageUpload(@RequestParam("image") MultipartFile file, @PathVariable String user) {
        return imageStorageService.store(file);
    }

    @GetMapping("{user}/image/{imageName}")
    //@RequiresAuthentication
    public Resource imageDownload(@PathVariable String user, @PathVariable String imageName) {
        return imageStorageService.loadAsResource(imageName);
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBean unauthorized() {
        return new ResponseBean(401, "Unauthorized401", null);
    }
}
