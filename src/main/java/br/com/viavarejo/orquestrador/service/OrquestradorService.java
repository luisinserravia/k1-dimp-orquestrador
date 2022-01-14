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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
	
	public String FORMATO_DATA = "dd/MM/yyyy HH:mm:ss";
	
	private List<String> controle;
	private int ponteiro;
	private int ponteiroLote;
	private List<String> tabelas;
	private Map<String, String> mapaTabelas;
	private Map<String, Integer> mapaTamanhoTabelas;
	private Map<String, String> prefixoTabelas;
	
	private Integer indiceLote;
	private Integer loteOrquestrador;
	private String situacao = "iniciando";
	private int conta;
	private String inicial;
	private String finalizado;
	

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
		String[] tabs = System.getenv("DIMP_TABELAS").split(",");
		controle = Arrays.asList(tabs);
		/*
		controle.add("TbpagamentoTipo");
		controle.add("TbMeioCaptura");
		controle.add("TbIntermediadorServico");
		controle.add("TbPessoa");
		controle.add("TbEndereco");
		controle.add("TbSeller");
		controle.add("TbNotaFiscal");
		controle.add("TbCliente");
		controle.add("TbCancelamentoPagamento");
		controle.add("TbVenda");
		controle.add("TbPagamento");
		 */
		
		String inicial = new SimpleDateFormat(FORMATO_DATA).format(new Date());
		setInicial(inicial);
		
		credential = new StorageSharedKeyCredential(accountName, accoutKey);
		String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
		storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
		
		mapaTabelas = new HashMap<String, String>();
		prefixoTabelas = new HashMap<String, String>();
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
					prefixoTabelas.put(nomeTabela, partes[0]);
//					try {
//						FileOutputStream fobj1=new FileOutputStream(partes[2]);
//						BlockBlobClient bCliente = blobContainerClient.getBlobClient(nomeBlob).getBlockBlobClient();
//						int dataSize = (int) bCliente.getProperties().getBlobSize();
//						ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
//						bCliente.download(outputStream);
//						outputStream.writeTo(fobj1);
//						outputStream.flush();
//						outputStream.close();
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				}
			});
		
		tabelas = new ArrayList<>();
		for (Map.Entry<String, String> m : mapaTabelas.entrySet()) {
			tabelas.add(m.getKey());
		}
		for (String t: tabelas) {
			if (getControle().contains(t)) {
				String pfx = getPrefixoTabelas().get(t);
				String sfx = getMapaTabelas().get(t);
				String nomeBlob = pfx + "/" + t + "/" + sfx;
				try {
					FileOutputStream fobj1=new FileOutputStream(sfx);
					BlockBlobClient bCliente = blobContainerClient.getBlobClient(nomeBlob).getBlockBlobClient();
					int dataSize = (int) bCliente.getProperties().getBlobSize();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
					bCliente.download(outputStream);
					outputStream.writeTo(fobj1);
					outputStream.flush();
					outputStream.close();
				} catch (FileNotFoundException e) {
//					e.printStackTrace();
					logger.info("Saltando " + sfx);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
//		tabelas = retorno;
		setPonteiro(0);
		setPonteiroLote(0);
		setLoteOrquestrador(Integer.parseInt(System.getenv("DIMP_LOTE_ORQUESTRADOR"), 10));
		mapaTamanhoTabelas = new HashMap<>();
		calculaTamanhoTabelas();
	}
	
	public void calculaTamanhoTabelas() {
		int conta = 0;
		for (String tabela: getTabelas()) {
			conta++;
			logger.info("Calculando tamanho " + tabela + " (" + conta + " de " + getTabelas().size() +")");
			int tamanhoTabela = getTamanhoTabela(tabela);
			mapaTamanhoTabelas.put(tabela, tamanhoTabela);
		}
		logger.info("pronto");
		setSituacao("pronto");
		String finalizado = new SimpleDateFormat(FORMATO_DATA).format(new Date());
		setFinalizado(finalizado);
		JSONObject jsonTempo = exibeTempoLote(inicial, finalizado);
		String msg = "Finalizado preparo do orquestrador em " + jsonTempo.get("minutos").toString() + ":" + jsonTempo.get("segundos").toString() + " minutos";
		logger.info(msg);
	}
	
	public Itens getTabela() {
		Itens item = new Itens();
		boolean permanece = true;
		while (permanece) {
			if (getPonteiro() < getTabelas().size()) {
				int ponteiro = getPonteiro();
				String tabela = getTabelas().get(ponteiro);
				JSONObject infoTabela = getInfoTabela(tabela);
//				ponteiro++;
//				setPonteiro(ponteiro);
				if (getControle().contains(tabela)) {
					int nLotes = (int) infoTabela.get("nLotes");
					int qLote = getPonteiroLote() + 1;
					if (qLote > nLotes) {
						ponteiro++;
						setPonteiro(ponteiro);
						setPonteiroLote(0);
					} else {
						item = montaItem(tabela);
						setPonteiroLote(getPonteiroLote() + 1);
						permanece = false;
					}
				} else {
					ponteiro++;
					setPonteiro(ponteiro);
					setPonteiroLote(0);
				}
			} else {
				permanece = false;
			}
		}
		return item;
	}
	
	public Itens montaItem(String tabela) {
		Itens item = new Itens();
		item.setNome(tabela);
		item.setArquivo(mapaTabelas.get(tabela));
		int salta = 0;
		int idx = getPonteiroLote();
		int tamanhoTabela = (int) getInfoTabela(tabela).get("tamanho");
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
		item = montaItem(tabela);
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
		int zero = 0;
		if (getMapaTamanhoTabelas().containsKey(tabela)) {
			return mapaTamanhoTabelas.get(tabela);
		} else {
			int conta = 0;
			try (BufferedReader reader = Files.newBufferedReader(
					Paths.get(mapaTabelas.get(tabela)), StandardCharsets.ISO_8859_1)) {
				while (reader.readLine() != null) {
					conta++;
//					mapaTamanhoTabelas.put(tabela, conta);
				}
				return conta;
			} catch (Exception e) {
//				e.printStackTrace();
				return zero;
			}
		}
//		return mapaTamanhoTabelas.get(tabela);
	}
	
	public JSONObject getInfoTabela(String tabela) {
		Map<String, Integer> mapaTabela = new HashMap<>();
		if (!getSituacao().equalsIgnoreCase("pronto") || !getMapaTamanhoTabelas().containsKey(tabela)) {
			mapaTabela.put("tamanho", 0);
			mapaTabela.put("lote", 0);
			mapaTabela.put("nLotes", 0);
		} else {
			int tam = getTamanhoTabela(tabela);
			int tamLote = getLoteOrquestrador();
			int nLotes = tam / tamLote;
			if (tam % tamLote > 0) {
				nLotes++;
			}
			mapaTabela.put("tamanho", tam);
			mapaTabela.put("lote", tamLote);
			mapaTabela.put("nLotes", nLotes);
		}
		return new JSONObject(mapaTabela);
	}
	
	public JSONObject exibeTempoLote(String inicial, String finalizado) {
		LocalDateTime t1 = LocalDateTime.parse(inicial, new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toFormatter());
		LocalDateTime t2 = LocalDateTime.parse(finalizado, new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toFormatter());
		long minutos = t1.until( t2, ChronoUnit.MINUTES);
		long segundos = t1.until( t2, ChronoUnit.SECONDS);
		segundos = segundos % 60;
		logger.info("Registros tratados de "+inicial+" até "+finalizado+", rodou em "+minutos+":"+segundos+" minutos");
		Map<String, Long> mapa = new HashMap<String, Long>();
		mapa.put("minutos", minutos);
		mapa.put("segundos", segundos);
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

	/**
	 * @return the ponteiroLote
	 */
	public int getPonteiroLote() {
		return ponteiroLote;
	}

	/**
	 * @param ponteiroLote the ponteiroLote to set
	 */
	public void setPonteiroLote(int ponteiroLote) {
		this.ponteiroLote = ponteiroLote;
	}

	/**
	 * @return the inicial
	 */
	public String getInicial() {
		return inicial;
	}

	/**
	 * @param inicial the inicial to set
	 */
	public void setInicial(String inicial) {
		this.inicial = inicial;
	}

	/**
	 * @return the finalizado
	 */
	public String getFinalizado() {
		return finalizado;
	}

	/**
	 * @param finalizado the finalizado to set
	 */
	public void setFinalizado(String finalizado) {
		this.finalizado = finalizado;
	}

	/**
	 * @return the prefixoTabelas
	 */
	public Map<String, String> getPrefixoTabelas() {
		return prefixoTabelas;
	}

	/**
	 * @param prefixoTabelas the prefixoTabelas to set
	 */
	public void setPrefixoTabelas(Map<String, String> prefixoTabelas) {
		this.prefixoTabelas = prefixoTabelas;
	}

	
}
