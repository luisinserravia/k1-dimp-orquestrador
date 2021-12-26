package br.com.viavarejo.orquestrador.model;

import java.util.List;

/**
 * @author Luis Inserra
 *
 * 22 de dez. de 2021 16:38:32
 */
public class Itens {

	private String nome;
	private String arquivo;
	private Integer idx;
	private List<String> linhas;
	private int tamanhoTabela;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getArquivo() {
		return arquivo;
	}

	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}

	/**
	 * @return the idx
	 */
	public Integer getIdx() {
		return idx;
	}

	/**
	 * @param idx the idx to set
	 */
	public void setIdx(Integer idx) {
		this.idx = idx;
	}

	/**
	 * @return the linhas
	 */
	public List<String> getLinhas() {
		return linhas;
	}

	/**
	 * @param linhas the linhas to set
	 */
	public void setLinhas(List<String> linhas) {
		this.linhas = linhas;
	}

	/**
	 * @return the tamanhoTabela
	 */
	public int getTamanhoTabela() {
		return tamanhoTabela;
	}

	/**
	 * @param tamanhoTabela the tamanhoTabela to set
	 */
	public void setTamanhoTabela(int tamanhoTabela) {
		this.tamanhoTabela = tamanhoTabela;
	}
}
