package br.com.viavarejo.orquestrador.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;

import br.com.viavarejo.orquestrador.model.Itens;

/**
 * @author Luis Inserra
 *
 * 10 de dez. de 2021 15:46:12
 */
@Service
public class OrquestradorService {
	
	Logger logger = LogManager.getLogger(this.getClass());
	
	private List<String> controle;
	private int ponteiro;
	private List<String> tabelas;
	private Map<String, String> mapaTabelas;
	private Map<String, Integer> mapaTamanhoTabelas;
	
	private Integer indiceLote;
	private Integer loteOrquestrador;
	private String situacao = "iniciando";
	private int conta;
	

	final String accountName = "stgdimphlg";
	final String accoutKey = "aYjR6uZXQv+m4QCOKH3EDgFRMbHdMo9zg0D4fcDyhJy92fYgsuz7dQb65/ZHrCOpHELl5so0njKuCTeeb2tmkA==";
	StorageSharedKeyCredential credential;
	String endpoint;
	BlobServiceClient storageClient;
	BlobContainerClient blobContainerClient;
	
	public OrquestradorService() {
		super();
	}
	
	public void inicializaServico() {
		controle = new ArrayList<>();
		controle.add("TbpagamentoTipo");
		controle.add("TbMeioCaptura");
		controle.add("TbIntermediadorServico");
		controle.add("TbPessoa");
		controle.add("TbEndereco");
		controle.add("TbSeller");
		controle.add("TbNotaFiscal");
		controle.add("TbCliente");
		controle.add("TbCancelamentoPagamento");
		
		List<String> retorno = new ArrayList<>();
		credential = new StorageSharedKeyCredential(accountName, accoutKey);
		String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
		storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
		
		mapaTabelas = new HashMap<String, String>();
		blobContainerClient = storageClient.getBlobContainerClient("dimp-bi-hlg");
		setConta(0);
		blobContainerClient.listBlobs().forEach(blobItem -> {
			setConta(getConta() + 1);
			String nomeBlob = blobItem.getName();
				System.out.println("Mostrando: " + nomeBlob);
				String[] partes = nomeBlob.split("/");
				if (partes.length == 3) {
					logger.info("trazendo " + getConta() + " de 30...");
					String nomeTabela = partes[1];
					mapaTabelas.put(nomeTabela, partes[2]);
					retorno.add(nomeTabela);
					try {
						FileOutputStream fobj1=new FileOutputStream(partes[2]);
						BlockBlobClient bCliente = blobContainerClient.getBlobClient(nomeBlob).getBlockBlobClient();
						int dataSize = (int) bCliente.getProperties().getBlobSize();
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
						bCliente.download(outputStream);
						outputStream.writeTo(fobj1);
						outputStream.flush();
						outputStream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		
		tabelas = retorno;
		setSituacao("pronto");
		setPonteiro(0);
		setLoteOrquestrador(Integer.parseInt(System.getenv("DIMP_LOTE_ORQUESTRADOR"), 10));
		mapaTamanhoTabelas = new HashMap<>();
	}
	
	public Itens getTabela(Integer idx) {
		Itens item = new Itens();
		boolean permanece = true;
		while (permanece) {
			if (getPonteiro() < getTabelas().size()) {
				int ponteiro = getPonteiro();
				String tabela = getTabelas().get(ponteiro);
				ponteiro++;
				setPonteiro(ponteiro);
				if (getControle().contains(tabela)) {
					item = montaItem(tabela, idx, 0);
					permanece = false;
				}
			} else {
				permanece = false;
			}
		}
		return item;
	}
	
	public Itens montaItem(String tabela, int idx, int tamanhoTabela) {
		Itens item = new Itens();
		item.setNome(tabela);
		item.setArquivo(mapaTabelas.get(tabela));
		int salta = 0;
		try (BufferedReader reader = Files.newBufferedReader(
				Paths.get(mapaTabelas.get(tabela)), StandardCharsets.ISO_8859_1)) {
			if (idx == 0) {
				salta = 1;
			} else {
				salta = idx * getLoteOrquestrador();
			}
			for (int i=0; i< salta; i++) {
				reader.readLine();
			}
			int limite = salta + getLoteOrquestrador();
			limite = getLoteOrquestrador();
			int nLinhas = tamanhoTabela;
			if (nLinhas == 0) {
				nLinhas = getTamanhoTabela(tabela);
			}
			item.setTamanhoTabela(nLinhas);
			if (limite > nLinhas) {
				limite = nLinhas;
			}
			if (idx == 0) {
				if (getTamanhoTabela(tabela) > getLoteOrquestrador()) {
					limite--;
				}
			}
			setConta(0);
			String linha = reader.readLine();
			List<String> linhas = new ArrayList<>();
			while (linha != null && conta < limite) {
				conta++;
				linhas.add(linha);
				linha = reader.readLine();
			}
			item.setLinhas(linhas);
			if (conta == 0) {
				idx=-1;
			}
			item.setIdx(idx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}
	
	public Itens getLoteTabela(String tabela, int idx, int tamanhoTabela) {
		Itens item = new Itens();
		item = montaItem(tabela, idx, tamanhoTabela);
		return item;
	}
	
	public List<Itens> listaTabelas() {
		List<String> lista = getTabelas();
		List<Itens> itens = new ArrayList<>();
		for (int i=0; i< lista.size(); i++) {
			Itens item = new Itens();
			String nome = lista.get(i);
			item.setNome(nome);
			item.setArquivo(mapaTabelas.get(nome));
			itens.add(item);
		}
		return itens;
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
		String tabela = getTabelas().get(ponteiro);
		/*
		if (ponteiro > 0) {
			ponteiro--;
		}
		String tabela =  getControle().get(ponteiro);
		*/
		Map<String, String> mapa = new HashMap<>();
		mapa.put("resposta", tabela);
		return new JSONObject(mapa);
	}
	
	private int getTamanhoTabela(String tabela) {
		if (getMapaTamanhoTabelas().containsKey(tabela)) {
			return mapaTamanhoTabelas.get(tabela);
		} else {
			int conta = 0;
			try (BufferedReader reader = Files.newBufferedReader(
					Paths.get(mapaTabelas.get(tabela)), StandardCharsets.ISO_8859_1)) {
				while (reader.readLine() != null) {
					conta++;
					mapaTamanhoTabelas.put(tabela, conta);
				}
				return conta;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mapaTamanhoTabelas.get(tabela);
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

	public List<String> getTabelas() {
		return tabelas;
	}

	public void setTabelas(List<String> tabelas) {
		this.tabelas = tabelas;
	}

	public Map<String, String> getMapaTabelas() {
		return mapaTabelas;
	}

	public void setMapaTabelas(Map<String, String> mapaTabelas) {
		this.mapaTabelas = mapaTabelas;
	}

	/**
	 * @return the situacao
	 */
	public String getSituacao() {
		return situacao;
	}

	/**
	 * @param situacao the situacao to set
	 */
	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}

	/**
	 * @return the conta
	 */
	public int getConta() {
		return conta;
	}

	/**
	 * @param conta the conta to set
	 */
	public void setConta(int conta) {
		this.conta = conta;
	}

	/**
	 * @return the indiceLote
	 */
	public Integer getIndiceLote() {
		return indiceLote;
	}

	/**
	 * @param indiceLote the indiceLote to set
	 */
	public void setIndiceLote(Integer indiceLote) {
		this.indiceLote = indiceLote;
	}

	/**
	 * @return the loteOrquestrador
	 */
	public Integer getLoteOrquestrador() {
		return loteOrquestrador;
	}

	/**
	 * @param loteOrquestrador the loteOrquestrador to set
	 */
	public void setLoteOrquestrador(Integer loteOrquestrador) {
		this.loteOrquestrador = loteOrquestrador;
	}

	/**
	 * @return the mapaTamanhoTabelas
	 */
	public Map<String, Integer> getMapaTamanhoTabelas() {
		return mapaTamanhoTabelas;
	}

	/**
	 * @param mapaTamanhoTabelas the mapaTamanhoTabelas to set
	 */
	public void setMapaTamanhoTabelas(Map<String, Integer> mapaTamanhoTabelas) {
		this.mapaTamanhoTabelas = mapaTamanhoTabelas;
	}

	
}
