package sandbox;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BackendApiClient {
    @GET("/api/stickers")
    Single<List<Sticker>> getStickers(@Query("num") int num);

    @GET("/api/emojis")
    Single<List<Emoji>> getEmojis(@Query("num") int num);

    @GET("/api/games")
    Single<List<Game>> getGames(@Query("num") int num);
}
