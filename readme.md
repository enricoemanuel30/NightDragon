# 🐉 NIGHT DRAGON

*The night has its own punishment.*

## 🎮 Sobre o jogo

**Night Dragon** é um jogo 2D do gênero Beat 'em Up, desenvolvido em Java como projeto acadêmico.

O jogador assume o controle de **Dragon of Night**, um combatente com um passado marcado pela violência, que precisará enfrentar uma organização conhecida como Devil Cowboys.

O projeto busca combinar combates intensos, personagens marcantes e uma narrativa ambientada no universo de *O Dragão da Noite*.

## 📖 História

Uma organização conhecida como **Devil Cowboys** está realizando experimentos obscuros envolvendo modificações mecânicas no corpo humano.

Seus integrantes passam por transformações que os tornam verdadeiras armas vivas, misturando tecnologia, violência e rituais sinistros.

Diante dessa ameaça, Dragon of Night decide enfrentar a organização e descobrir o que está por trás de suas atividades.

## 🐉 Protagonista

### Dragon of Night

Conhecido por sua personalidade intimidadora e habilidades de combate, Dragon of Night é um homem marcado por seu passado.

Sua jornada é movida por confrontos, mistérios e pela busca de redenção.

## 🤠 Inimigos — Devil Cowboys

Os Devil Cowboys são membros de uma seita que utiliza modificações mecânicas para transformar seus integrantes em combatentes perigosos.

O jogo contará inicialmente com três tipos de inimigos:

- **Devil Cowboy Grunt:** combatente básico, especializado em ataques corpo a corpo.
- **Devil Cowboy Gunner:** inimigo especializado em ataques à distância e armamentos.
- **Devil Cowboy Brute:** combatente pesado, com modificações mecânicas que aumentam sua força e resistência.

## 🚀 Novidades da Versão 3.0.0 (Mini Projeto 01)

A **v3.0.0** implementa a arquitetura de formas geométricas, áudio nativo, gerenciamento de placar e vidas:

- **🟡 Formas Geométricas & Coletáveis 2.5D (`WorldShape`):**
  - **Moedas de Ouro (Círculo):** coletáveis com rotação e flutuação que concedem +10 pontos e +5 de Fúria ao personagem. Também dropadas ao eliminar inimigos.
  - **Caixotes e Barris (Retângulo / Quadrado):** obstáculos interativos no piso do cenário com hitboxes calibradas e sombras em perspectiva.
- **🏆 Sistema de Placar & Pontuação:**
  - Exibição de pontuação dinâmica em tempo real no HUD e na tela de encerramento via `g2d.drawString()`.
  - Remoção segura de elementos coletados sem risco de `ConcurrentModificationException`.
- **❤️ Sistema de Vidas:**
  - Contador com 3 vidas iniciais exibido no HUD. Ao zerar o HP, uma vida é consumida e o personagem renasce com mensagem de aviso; Game Over somente após esgotar todas as vidas.
- **🔊 Gerenciador de Áudio Nativo (`SoundManager`):**
  - Efeitos sonoros `.wav` assíncronos (`javax.sound.sampled`) para socos, moedas, cambalhota e fúria, sem comprometer a fluidez da Thread principal a 60 FPS.
- **🎮 Controles & Execução:**
  - <kbd>W</kbd> <kbd>A</kbd> <kbd>S</kbd> <kbd>D</kbd> / Setas: Movimentação
  - <kbd>Espaço</kbd>: Cambalhota / Esquiva
  - <kbd>J</kbd>: Soco | <kbd>K</kbd>: Chute | <kbd>L</kbd>: Fúria do Dragão | <kbd>R</kbd>: Reiniciar
  - Execução rápida com `run.bat` ou Maven.
