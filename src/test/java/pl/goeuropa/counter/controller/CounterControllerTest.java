package pl.goeuropa.counter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.goeuropa.counter.dto.ArduinoDeviceSnapshotDto;
import pl.goeuropa.counter.dto.DeviceSummaryDto;
import pl.goeuropa.counter.dto.ScannedDeviceDto;
import pl.goeuropa.counter.service.CounterService;

import java.util.List;
import java.util.Map;

import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CounterController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CounterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CounterService counterService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDevices_returnsEmptyMap() throws Exception {
        when(counterService.getDeviceSnapshots()).thenReturn(Map.of());

        mockMvc.perform(get("/v2/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{}"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDevices_returnsSnapshotMap() throws Exception {
        ArduinoDeviceSnapshotDto snapshot = buildSnapshot("ESP32_AABBCC");
        when(counterService.getDeviceSnapshots()).thenReturn(Map.of("ESP32_AABBCC", snapshot));

        mockMvc.perform(get("/v2/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ESP32_AABBCC.device_id").value("ESP32_AABBCC"))
                .andExpect(jsonPath("$.ESP32_AABBCC.summary.total_devices").value(12))
                .andExpect(jsonPath("$.ESP32_AABBCC.devices[0].mac").value("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void postDevice_savesSnapshotAndReturnsConfirmation() throws Exception {
        ArduinoDeviceSnapshotDto snapshot = buildSnapshot("ESP32_AABBCC");

        mockMvc.perform(post("/v2/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snapshot)))
                .andExpect(status().isOk())
                .andExpect(content().string("Snapshot from ESP32_AABBCC saved."));

        verify(counterService, times(1)).saveDeviceSnapshot(any(ArduinoDeviceSnapshotDto.class));
    }

    @Test
    void postDevice_withUnknownFields_isIgnored() throws Exception {
        String json = """
                {
                  "device_id": "ESP32_TEST",
                  "timestamp": 1704246600,
                  "unknown_field": "ignored",
                  "devices": [],
                  "summary": {"total_devices": 0}
                }
                """;

        mockMvc.perform(post("/v2/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Snapshot from ESP32_TEST saved."));
    }

    private ArduinoDeviceSnapshotDto buildSnapshot(String deviceId) {
        ScannedDeviceDto device = new ScannedDeviceDto();
        device.setMac("AA:BB:CC:DD:EE:FF");
        device.setRssi(-65);
        device.setDistance(2.5);
        device.setName("iPhone");
        device.setCategory("phone");
        device.setAddressType("random");
        device.setSeenCount(15);
        device.setFirstSeen(1704246000L);
        device.setLastSeen(1704246590L);

        DeviceSummaryDto summary = new DeviceSummaryDto();
        summary.setTotalDevices(12);
        summary.setPhones(5);
        summary.setComputers(2);
        summary.setWearables(1);
        summary.setBeacons(3);
        summary.setOther(1);

        ArduinoDeviceSnapshotDto snapshot = new ArduinoDeviceSnapshotDto();
        snapshot.setDeviceId(deviceId);
        snapshot.setTimestamp(1704246600L);
        snapshot.setDevices(List.of(device));
        snapshot.setSummary(summary);
        return snapshot;
    }
}
