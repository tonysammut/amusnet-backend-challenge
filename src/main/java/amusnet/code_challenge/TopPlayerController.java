package amusnet.code_challenge;

import java.util.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import amusnet.code_challenge.model.GameActivity;
import amusnet.code_challenge.model.Player;

@RestController
public class TopPlayerController {
	private final RestTemplate restTemplate;
	private final RetryTemplate retryTemplate;
	private final AgsApiProxy agsApiProxy;

	public TopPlayerController() {
		this.restTemplate = new RestTemplate();
		this.retryTemplate = createRetryTemplate();
		this.agsApiProxy = new AgsApiProxy(restTemplate);
	}

	@GetMapping("/top-player")
	private Player topPlayer(String name) throws Exception {
		// Get list of 30 players
		Player[] players = retryTemplate.execute((context -> {
			return agsApiProxy.fetchPlayers(30);
		}));
		if (players.length == 0) {
			System.out.println("No players found");
			return null;
		}

		// Get activity for all 30 players and their 20 entries
		GameActivity[] activity = agsApiProxy.fetchActivity(players, players.length * 20);
		if (activity.length == 0) {
			System.out.println("No activity found");
			return null;
		}

		// Group by player and calculate total GRR for each player
		Map<Long, Double> activityMap = new HashMap<>();
		for (GameActivity ga : activity) {
			Long playerId = ga.getPlayerId();

			if (activityMap.containsKey(playerId)) {
				activityMap.put(playerId, activityMap.get(playerId) + ga.getGRR());
			} else {
				activityMap.put(playerId, ga.getGRR());
			}
		}

		// Sort by GRR desc
		List<Map.Entry<Long, Double>> sortedList = new ArrayList<>(activityMap.entrySet());
		sortedList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

		// Fetch first player
		Long topPlayerId = sortedList.get(0).getKey();
		// Find the player from players whose id matches topPlayer
		Player topPlayer = Arrays.stream(players).filter(p -> p.getId().equals(topPlayerId)).findFirst()
				.orElse(null);

		System.out.println("Top Player: " + topPlayer.getName() + " with GRR: " + sortedList.get(0).getValue());
		return topPlayer;
	}

	private RetryTemplate createRetryTemplate() {

		RetryTemplate retryTemplate = new RetryTemplate();

		// SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
		// simpleRetryPolicy.setMaxAttempts(3);
		// retryTemplate.setRetryPolicy(simpleRetryPolicy);

		// ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		// backOffPolicy.setInitialInterval(1000);
		// retryTemplate.setBackOffPolicy(backOffPolicy);

		return retryTemplate;
	}
}