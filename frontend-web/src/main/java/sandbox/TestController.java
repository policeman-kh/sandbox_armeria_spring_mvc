package sandbox;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import io.reactivex.rxjava3.core.Single;
import sandbox.configuration.ClientConfiguration.ForLoadBalancingClient;

@Controller
public class TestController {
    private final BackendApiClient backendApiClient;

    public TestController(@ForLoadBalancingClient BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    @GetMapping
    public ModelAndView home() {
        return Single.zip(backendApiClient.getStickers(2),
                          backendApiClient.getEmojis(2),
                          backendApiClient.getGames(2),
                          (stickers, emojis, games) ->
                                  new ModelAndView("home")
                                          .addObject("stickers", stickers)
                                          .addObject("emojis", emojis)
                                          .addObject("games", games))
                     .blockingGet();
    }
}
