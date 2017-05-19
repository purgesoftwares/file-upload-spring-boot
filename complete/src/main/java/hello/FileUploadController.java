package hello;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String filename = new Date().getTime() + "-" + file.getOriginalFilename();
        storageService.store(file, filename);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @PostMapping("/upload")
    @ResponseBody
    @CrossOrigin
    public String handleAPIFileUpload(@RequestParam("file") MultipartFile file) {

        String filename = new Date().getTime() + "-" + file.getOriginalFilename();

        /*String filename = new Date().getTime() + "-" + file.getOriginalFilename()
         String filename = new Date().getTime() + "-" + file.getOriginalFilename();

        if (!file.isEmpty()) {
            try {

                //This will be replaced when multipart data file request is working.
                File outputFile = applicationContext.getResource("/resources/uploads/" + filename).getFile();

                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(outputFile));
                stream.write(bytes);
                stream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
        storageService.store(file, filename);
        //Resource file1 = storageService.loadAsResource(file.getOriginalFilename());
        return filename;
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
