package com.company.hr.controller;

import com.company.hr.service.AdmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/iclock")
@RequiredArgsConstructor
public class AdmsController {

    private final AdmsService admsService;

    @GetMapping(value = "/cdata", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handshake(@RequestParam("SN") String serialNumber) {
        if (!admsService.isDeviceAllowed(serialNumber)) {
            log.warn("ADMS handshake rejected for unknown device SN={}", serialNumber);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        String config = String.join("\n",
                "GET OPTION FROM: " + serialNumber,
                "ATTLOGStamp=None",
                "OPERLOGStamp=9999",
                "ATTPHOTOStamp=None",
                "ErrorDelay=30",
                "Delay=30",
                "TransTimes=00:00;14:00",
                "TransInterval=1",
                "TransFlag=TransData AttLog",
                "Realtime=1",
                "Encrypt=0");
        return ResponseEntity.ok(config);
    }

    @PostMapping(value = "/cdata", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receive(@RequestParam("SN") String serialNumber,
                                          @RequestParam(value = "table", required = false) String table,
                                          @RequestBody(required = false) String body) {
        if (!admsService.isDeviceAllowed(serialNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        if (table == null || "ATTLOG".equalsIgnoreCase(table)) {
            admsService.processAttLog(serialNumber, body);
        } else {
            log.debug("ADMS [{}]: table '{}' ignored", serialNumber, table);
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "/getrequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRequest(@RequestParam(value = "SN", required = false) String serialNumber) {
        return ResponseEntity.ok("OK");
    }
}
