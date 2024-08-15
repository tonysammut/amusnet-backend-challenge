/**
 * AgsApiProxy -> Amusnet Gaming Services API Proxy
 */
package amusnet.code_challenge;

import java.time.Instant;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import amusnet.code_challenge.model.GameActivity;
import amusnet.code_challenge.model.Player;

public class AgsApiProxy {
	public static final String BASEURL = "https://challenge.dev.amusnetgaming.net";
	private final RestTemplate restTemplate;

	public AgsApiProxy(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * Fetch players from the API
	 * @param limit Number of players to fetch
	 * @return {@code Player[]} Array of players
	 * @throws Exception
	 */
	@Retryable(value = {
			HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000), recover = "fallbackFetchPlayers")
	public Player[] fetchPlayers(int limit) throws Exception {
		System.out.println("Fetching players...");

		String qs = "page=0&pageSize=" + limit;

		try {
			Player[] players = restTemplate.getForObject(
					BASEURL + "/players?" + qs, Player[].class);
			if (players == null) {
				throw new Exception("No players found");
			}
			System.out.println("players.length: " + players.length);

			return players;
		} catch (HttpServerErrorException e) {
			System.out.println("Retry due to http server error at: " + Instant.now());
			throw e;
		}
	}

	/**
	 * Called if all retries fail
	 * @param e
	 * @param limit The same limit that was passed to fetchPlayers
	 * @return An empty array instead of an exception so that the application can continue
	 */
	@Recover
	private Player[] fallbackFetchPlayers(Exception e, int limit) {
		System.out.println("Fallback due to exception: " + e.getMessage());
		return new Player[0];
	}

	/**
	 * Fetch game activity for the given players
	 * @param players Players to fetch activity for
	 * @param pageSize Number of activities to fetch
	 * @return {@code GameActivity[]} Array of game activities
	 * @throws Exception
	 */
	@Retryable(value = {
			HttpServerErrorException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000), recover = "fallbackFetchGameActivity")
	public GameActivity[] fetchActivity(Player[] players, int pageSize) throws Exception {
		System.out.println("Fetching activity...");

		String qs = "page=0&pageSize=" + pageSize;
		for (Player player : players) {
			qs += "&playerIds=" + player.getId();
		}

		try {
			GameActivity[] activity = restTemplate.getForObject(
					AgsApiProxy.BASEURL + "/game-activity?" + qs, GameActivity[].class);
			if (activity == null) {
				throw new Exception("No activity found");
			}
			System.out.println("activity.length: " + activity.length);

			return activity;
		} catch (HttpServerErrorException e) {
			System.out.println("Retry due to http server error at: " + Instant.now());
			throw e;
		}
	}

	/**
	 * Called if all retries fail
	 * @param e
	 * @param players The same players that were passed to fetchActivity
	 * @param pageSize The same limit that was passed to fetchActivity
	 * @return An empty array instead of an exception so that the application can continue
	 */
	@Recover
	private GameActivity[] fallbackFetchGameActivity(Exception e, Player[] players, int pageSize) {
		System.out.println("Fallback due to exception: " + e.getMessage());
		return new GameActivity[0];
	}
}