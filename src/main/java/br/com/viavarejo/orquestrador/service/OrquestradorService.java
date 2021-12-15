package br.com.viavarejo.orquestrador.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author Luis Inserra
 *
 * 10 de dez. de 2021 15:46:12
 */
@Service
public class OrquestradorService {
	
	private List<String> controle;
	private int ponteiro;

	public OrquestradorService() {
		super();
		controle = new ArrayList<>();
		controle.add("tabelas/TbpagamentoTipo/");
		controle.add("tabelas/TbMeioCaptura/");
		controle.add("tabelas/TbIntermediadorServico/");
		controle.add("tabelas/TbPessoa/");
		controle.add("tabelas/TbEndereco/");
		controle.add("tabelas/TbSeller/");
		controle.add("tabelas/TbNotaFiscal/");
		controle.add("tabelas/TbCliente/");
		controle.add("tabelas/TbCancelamentoPagamento/");
		
		setPonteiro(0);
	}
	
	public String getTabela() {
		if (getPonteiro() < controle.size()) {
			int ponteiro = getPonteiro();
			String tabela = getControle().get(ponteiro);
			ponteiro++;
			setPonteiro(ponteiro);
			return tabela;
		} else {
			return "";
		}
	}
	
	public List<String> listaTabelas() {
		List<String> retorno = new ArrayList<>();
		for (int i=0; i< controle.size(); i++) {
			retorno.add(getControle().get(i));
		}
		return retorno;
	}
	
	public JSONObject inicializaPonteiro() {
		int zero = 0;
		setPonteiro(zero);
		Map<String, String> mapa = new HashMap<>();
		mapa.put("resposta", "Ok");
		return new JSONObject(mapa);
	}
	
	public JSONObject getTabelaCorrente() {
		int ponteiro = getPonteiro();
		if (ponteiro > 0) {
			ponteiro--;
		}
		String tabela =  getControle().get(ponteiro);
		Map<String, String> mapa = new HashMap<>();
		mapa.put("resposta", tabela);
		return new JSONObject(mapa);
	}

	public List<String> getControle() {
		return controle;
	}

	public void setControle(List<String> controle) {
		this.controle = controle;
	}

	public int getPonteiro() {
		return ponteiro;
	}

	public void setPonteiro(int ponteiro) {
		this.ponteiro = ponteiro;
	}

	
}
