package sandbox;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import sandbox.endpointgroup.OnMemoryDynamicEndpointGroup;

@AllArgsConstructor
@RestController
@RequestMapping("/setting")
public class SettingController {
    private final OnMemoryDynamicEndpointGroup endpointGroup;

    @GetMapping("/addEndpoint")
    public String addEndpoint() {
        endpointGroup.addEndpoint();
        return "success";
    }
}
