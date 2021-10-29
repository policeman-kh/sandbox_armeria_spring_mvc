package sandbox;

import java.util.List;

import org.springframework.stereotype.Service;

import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.PathPrefix;

import io.reactivex.rxjava3.core.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Service
@PathPrefix("/api")
public class BackendApiService {
    private static final List<Sticker> STICKERS =
            List.of(new Sticker("1","hoge sticker",""),
                    new Sticker("2","fuga sticker",""),
                    new Sticker("3","piyo sticker",""));
    private static final List<Emoji> EMOJIS =
            List.of(new Emoji("1","hoge emoji",""),
                    new Emoji("2","fuga emoji",""),
                    new Emoji("3","piyo emoji",""));
    private static final List<Game> GAMES =
            List.of(new Game("1","hoge game",""),
                    new Game("2","fuga game",""),
                    new Game("3","piyo game",""));

    @Get("/stickers")
    public Single<List<Sticker>> getStickers(@Param("num") int num) {
        return Single.just(STICKERS.subList(0, Math.min(num, STICKERS.size() -1)));
    }

    @Get("/emojis")
    public Single<List<Emoji>> getEmojis(@Param("num") int num) {
        return Single.just(EMOJIS.subList(0, Math.min(num, EMOJIS.size() -1)));
    }

    @Get("/games")
    public Single<List<Game>> getGames(@Param("num") int num) {
        return Single.just(GAMES.subList(0, Math.min(num, GAMES.size() -1)));
    }
}
