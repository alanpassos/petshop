package com.crowde.fenrir.storage.local;

import static java.nio.file.FileSystems.getDefault;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.crowde.fenrir.storage.FotoStorage;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

public class FotoStorageLocal implements FotoStorage {

	private static final Logger logger = LoggerFactory.getLogger(FotoStorageLocal.class);

	private Path local;
	private Path localTemporario;

	public FotoStorageLocal() {
		this(getDefault().getPath(System.getenv("HOME"), ".fenrirfotos"));
	}

	public FotoStorageLocal(Path path) {
		this.local = path;
		criarPastas();
	}

	@Override
	public String salvarTemporariamente(MultipartFile[] files) {
		String novoNome  = null;
		if (files != null && files.length > 0) {
			MultipartFile arquivo = files[0];
		novoNome = renomearArquivo(arquivo.getOriginalFilename());
			try {

				arquivo.transferTo(new File(
						this.localTemporario.toAbsolutePath().toString() + getDefault().getSeparator() + novoNome));
			} catch (IOException e) {

				throw new RuntimeException("Erro salvando a foto na pasta temporaria" + e);
			}
			
		}
		
		return novoNome;
		
	}

	@Override
	public byte[] recuperarFotoRemporaria(String nome) {
		
		try {
			return Files.readAllBytes(this.localTemporario.resolve(nome));
		} catch (IOException e) {

			throw new RuntimeException("Erro lendo foto temporária" + e);
		}
	}
	
	@Override
	public void salvar(String imagem) {
		try {
			Files.move(this.localTemporario.resolve(imagem), this.local.resolve(imagem));
		} catch (IOException e) {
			throw new RuntimeException("Erro Movendo foto para destino final" + e);
		}		
		
		try {
			Thumbnails.of(this.local.resolve(imagem).toString()).size(40, 68).toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Erro gerando Thumbnail" + e);
		}
	}
	
	
	@Override
	public byte[] recuperar(String nome) {
		try {
			return Files.readAllBytes(this.local.resolve(nome));
		} catch (IOException e) {

			throw new RuntimeException("Erro lendo foto temporária" + e);
		}
	}
	
	
	private void criarPastas() {
		try {
			Files.createDirectories(local);
			this.localTemporario = getDefault().getPath(this.local.toString(), "temp");
			Files.createDirectories(this.localTemporario);

			if (logger.isDebugEnabled()) {
				logger.debug("Pastas criadas para salvar fotos.");
				logger.debug("Pasta default:" + this.local.toAbsolutePath());
				logger.debug("Pastas temporaria: " + this.localTemporario.toAbsolutePath());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error Criando pastas para salvar fotos" + e);
		}
	}

	private String renomearArquivo(String nomeOriginal) {
		String novoNome = UUID.randomUUID().toString() + "_" + nomeOriginal;
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Nome Original: %s, Novo nome do arquivo %s", nomeOriginal, novoNome));
		}

		return novoNome;

	}

}
