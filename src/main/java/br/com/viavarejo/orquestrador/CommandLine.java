package br.com.viavarejo.orquestrador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.viavarejo.orquestrador.service.OrquestradorService;

/**
 * @author Luis Inserra
 *
 * 23 de dez. de 2021 10:58:01
 */
@Component
public class CommandLine implements CommandLineRunner {

	@Autowired
	OrquestradorService s;
	
	@Override
	public void run(String... args) throws Exception {
		s.inicializaServico();
	}

}
