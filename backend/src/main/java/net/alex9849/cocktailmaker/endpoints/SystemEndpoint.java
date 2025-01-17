package net.alex9849.cocktailmaker.endpoints;

import net.alex9849.cocktailmaker.model.Pump;
import net.alex9849.cocktailmaker.payload.dto.settings.ReversePumpingSettings;
import net.alex9849.cocktailmaker.service.PumpService;
import net.alex9849.cocktailmaker.service.PumpUpService;
import net.alex9849.cocktailmaker.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/system")
public class SystemEndpoint {

    @Autowired
    private SystemService systemService;

    @Autowired
    private PumpUpService pumpUpService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/pythonlibraries", method = RequestMethod.GET)
    public ResponseEntity<?> getPythonLibraries() throws IOException {
        return ResponseEntity.ok(systemService.getInstalledPythonLibraries());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/audiodevices", method = RequestMethod.GET)
    public ResponseEntity<?> getAudioDevices() throws IOException {
        return ResponseEntity.ok(systemService.getAudioDevices());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "settings/reversepumping", method = RequestMethod.PUT)
    public ResponseEntity<?> setReversePumpSettings(@RequestBody @Valid ReversePumpingSettings.Full settings) {
        if(settings.isEnable() && settings.getSettings() == null) {
            throw new IllegalStateException("Settings-Details are null!");
        }
        pumpUpService.setReversePumpingSettings(settings);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "settings/reversepumping", method = RequestMethod.GET)
    public ResponseEntity<?> getReversePumpSettings() {;
        return ResponseEntity.ok(pumpUpService.getReversePumpingSettings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/shutdown", method = RequestMethod.PUT)
    public ResponseEntity<?> shutdown() throws IOException {
        systemService.shutdown();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "settings/global", method = RequestMethod.GET)
    public ResponseEntity<?> getGlobalSettings() {;
        return ResponseEntity.ok(systemService.getGlobalSettings());
    }
}
