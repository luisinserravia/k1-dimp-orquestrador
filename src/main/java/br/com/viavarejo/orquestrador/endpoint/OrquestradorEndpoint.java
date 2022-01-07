package br.com.viavarejo.orquestrador.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import br.com.viavarejo.orquestrador.model.BlobsList;
import br.com.viavarejo.orquestrador.model.Itens;
import br.com.viavarejo.orquestrador.service.OrquestradorService;

/**
 * @author Luis Inserra
 *
 * 10 de dez. de 2021 15:55:52
 */
@RestController
public class OrquestradorEndpoint {

	@Autowired
	OrquestradorService s;
	
	@GetMapping("/")
	public String home() {
		return "Alô Mundo! DIMP databricks orquestrador está rodando";
	}
	
	@GetMapping("/getTabela")
	public String getTabela() {
		Itens item = s.getTabela();
		Gson gson = new Gson();
		String json = gson.toJson(item);
		return json;
	}
	
	@GetMapping(path = "/getLote", produces = MediaType.APPLICATION_JSON_VALUE)
	public Itens getLote(@RequestParam(value = "tabela", required = true) final String tabela,
			@RequestParam(value = "indice", required = true) final int indice,
			@RequestParam(value = "tamanhoTabela", required = true) final int tamanhoTabela) {
		return s.getLoteTabela(tabela, indice, tamanhoTabela);
	}
	
	@GetMapping("listaTabelas")
	public String listaTabelas() {
		List<Itens> itens = s.listaTabelas();
		Gson gson = new Gson();
		String json = gson.toJson(itens);
		return json;
	}
	
	@GetMapping(path = "/inicializaPonteiro", produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject inicializaPonteiro() {
		return s.inicializaPonteiro();
	}
	
	@GetMapping("/getTabelaCorrente")
	public JSONObject getTabelaCorrente() {
		return s.getTabelaCorrente();
	}
	
	@GetMapping(path = "/getSituacaoServico", produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject getSituacaoServico() {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("resposta", s.getSituacao());
		return new JSONObject(mapa);
	}
	
	@GetMapping(path = "/getInfoTabela", produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject getInfoTabela(@RequestParam(value = "tabela", required = true) final String tabela) {
		return s.getInfoTabela(tabela);
	}
}
