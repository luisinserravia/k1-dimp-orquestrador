package br.com.viavarejo.orquestrador.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public JSONObject getTabela() {
		String retorno = s.getTabela();
		Map<String, String> mapa = new HashMap<>();
		mapa.put("retorno", retorno);
		return new JSONObject(mapa);
	}
	
	@GetMapping("listaTabelas")
	public List<String> listaTabelas() {
		return s.listaTabelas();
	}
	
	@GetMapping("/inicializaPonteiro")
	public JSONObject inicializaPonteiro() {
		return s.inicializaPonteiro();
	}
	
	@GetMapping("/getTabelaCorrente")
	public JSONObject getTabelaCorrente() {
		return s.getTabelaCorrente();
	}
}
