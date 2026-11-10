package api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Pokemon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PokeApiClient {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private final HttpClient client;

    public PokeApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Pokemon buscarPokemon(String nomeOuId) throws Exception {
        // Limpa a entrada e formata a URL
        String busca = nomeOuId.trim().toLowerCase();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + busca))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // ---> ADICIONE ESTA LINHA PARA INSPECIONAR O RETORNO <---
        System.out.println("RESPOSTA RECEBIDA: \n" + response.body());

        // Verifica se a rede interceptou a chamada e devolveu um HTML de firewall
        if (response.body().trim().startsWith("<")) {
            throw new RuntimeException("Acesso bloqueado pelo firewall da rede local.");
        }

        if (response.statusCode() == 404) {
            throw new IllegalArgumentException("Pokémon não encontrado!");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Erro na API: Código " + response.statusCode());
        }

        return extrairDadosJson(response.body());
    }

    private Pokemon extrairDadosJson(String jsonBody) {
        JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();

        int id = json.get("id").getAsInt();
        // Capitaliza a primeira letra do nome
        String name = json.get("name").getAsString();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        // Altura e peso vêm em decímetros e hectogramas na API, convertendo para metros e kg
        double height = json.get("height").getAsDouble() / 10.0;
        double weight = json.get("weight").getAsDouble() / 10.0;

        // Extrai o primeiro tipo (ex: fire, water)
        String type = json.getAsJsonArray("types")
                .get(0).getAsJsonObject()
                .getAsJsonObject("type")
                .get("name").getAsString();

        // Extrai a URL da imagem (sprite oficial)
        String imageUrl = json.getAsJsonObject("sprites")
                .getAsJsonObject("other")
                .getAsJsonObject("official-artwork")
                .get("front_default").getAsString();

        return new Pokemon(id, name, height, weight, type, imageUrl);
    }
}