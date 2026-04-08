package dev.siample.dashboard.controller;

import dev.siample.dashboard.dto.DashboardStatusDto;
import dev.siample.dashboard.websocket.DashboardWebSocketHandler;
import dev.siample.dashboard.service.WebSocketStatusService;
import org.springframework.boot.info.BuildProperties;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class DashboardController {

    @Value("${app.environment.name}")
    private String environment;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final DashboardWebSocketHandler handler;
    private final WebSocketStatusService statusService;
    private final BuildProperties buildProperties;
    private DashboardStatusDto dashboardStatusDto;

    public DashboardController(DashboardWebSocketHandler handler, 
                               WebSocketStatusService statusService,
                               DashboardStatusDto dashboardStatusDto,
                               BuildProperties buildProperties) {
        this.handler = handler;
        this.statusService = statusService;
        this.dashboardStatusDto = dashboardStatusDto;
        this.buildProperties = buildProperties;
    }

    @GetMapping({"/dashboard", "/", "/index"})
    public String dashboard(Model model) {

        boolean wsConnected = handler.isConnected();
        model.addAttribute("wsConnected", wsConnected);

        model.addAttribute("environment", environment);
        model.addAttribute("frontendUrl", frontendUrl);
        model.addAttribute("username", "Guest (default)");
        model.addAttribute("serverTimeZone", dashboardStatusDto.getTheTimeZones());
        model.addAttribute("serverStartTime", dashboardStatusDto.getServerStartTimeAsString());
        model.addAttribute("version", buildProperties.getVersion());

        // Format build time: (2026.Apr.08.)
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy.MMM.dd.", Locale.ENGLISH);
        String buildDate = dayFormatter.format(buildProperties.getTime().atZone(ZoneId.systemDefault()));
        model.addAttribute("buildDate", buildDate);

        // Add message history
        model.addAttribute("messages", statusService.getCurrentMessagesHtml());
        model.addAttribute("messagesToArchive", statusService.getArchivedMessagesHtml());

        return "dashboard";
    }


}
