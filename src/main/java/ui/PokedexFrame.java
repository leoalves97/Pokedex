package ui;

import api.PokeApiClient;
import model.Pokemon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class PokedexFrame extends JFrame {

    private final JTextField campoBusca;
    private final JButton botaoBuscar;
    private final JLabel labelImagem;
    private final JLabel labelInfo;
    private final PokeApiClient apiClient;

    public PokedexFrame() {
        this.apiClient = new PokeApiClient();

        setTitle("Pokédex - Trabalho de Java");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Painel Superior (Busca)
        JPanel painelTopo = new JPanel(new FlowLayout());
        campoBusca = new JTextField(15);
        botaoBuscar = new JButton("Buscar");
        painelTopo.add(new JLabel("Nome ou ID:"));
        painelTopo.add(campoBusca);
        painelTopo.add(botaoBuscar);

        // Painel Central (Imagem)
        labelImagem = new JLabel("Digite um Pokémon e clique em Buscar", SwingConstants.CENTER);
        labelImagem.setPreferredSize(new Dimension(300, 300));

        // Painel Inferior (Informações)
        labelInfo = new JLabel("", SwingConstants.CENTER);
        labelInfo.setFont(new Font("Arial", Font.BOLD, 14));
        labelInfo.setPreferredSize(new Dimension(400, 100));

        add(painelTopo, BorderLayout.NORTH);
        add(labelImagem, BorderLayout.CENTER);
        add(labelInfo, BorderLayout.SOUTH);

        // Ação do botão
        botaoBuscar.addActionListener(e -> pesquisar());
        campoBusca.addActionListener(e -> pesquisar()); // Permite buscar apertando Enter
    }

    private void pesquisar() {
        String texto = campoBusca.getText();
        if (texto.isBlank()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite um nome ou ID.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Estado de carregamento
        botaoBuscar.setEnabled(false);
        labelInfo.setText("Buscando dados na API...");
        labelImagem.setIcon(null);
        labelImagem.setText("Carregando...");

        // Executa a chamada HTTP em uma Thread separada (Evita travar a interface)
        SwingWorker<Pokemon, Void> worker = new SwingWorker<>() {
            private BufferedImage imagemBaixada;

            @Override
            protected Pokemon doInBackground() throws Exception {
                Pokemon pkmn = apiClient.buscarPokemon(texto);
                // Baixa a imagem em background
                URL url = java.net.URI.create(pkmn.imageUrl()).toURL();
                imagemBaixada = ImageIO.read(url);
                return pkmn;
            }

            @Override
            protected void done() {
                try {
                    Pokemon pkmn = get();
                    // Redimensiona a imagem para caber na tela
                    Image imgRedimensionada = imagemBaixada.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                    labelImagem.setIcon(new ImageIcon(imgRedimensionada));
                    labelImagem.setText(null);

                    // Atualiza os textos
                    String infoHtml = String.format("<html><center><b>Nº %d - %s</b><br>Tipo: %s<br>Altura: %.1f m<br>Peso: %.1f kg</center></html>",
                            pkmn.id(), pkmn.name(), pkmn.type().toUpperCase(), pkmn.height(), pkmn.weight());
                    labelInfo.setText(infoHtml);

                } catch (Exception ex) {
                    ex.printStackTrace(); // <-- Adicione esta linha exata
                    labelImagem.setText("Nenhuma imagem");
                    labelInfo.setText("");
                    String erro = ex.getMessage().contains("Pokémon não encontrado") ? "Pokémon não encontrado!" : "Erro de conexão.";
                    JOptionPane.showMessageDialog(PokedexFrame.this, erro, "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    botaoBuscar.setEnabled(true);
                    campoBusca.selectAll();
                }
            }
        };
        worker.execute();
    }
}