    /**
     * Processa a movimentação do jogador baseada nas teclas direcionais.
     * Atualiza o estado de caminhada e restringe movimentos durante ataques normais.
     * 
     * @param dx Direção no eixo horizontal (-1 = esquerda, 0 = parado, +1 = direita)
     * @param dy Direção no eixo vertical (-1 = sobe, 0 = parado, +1 = desce)
     */
    public void move(int dx, int dy) {
        // Registra se houve intenção de movimento no frame atual
        this.isMoving = (dx != 0 || dy != 0);

        // Se estiver desferindo um ataque normal, trava a movimentação para dar peso e impacto ao golpe
        // (A menos que esteja no Modo Fúria, que permite deslizar golpeando)
        if (attacking && !isFuriaActive) return;

        // Deslocamento horizontal e atualização da orientação do olhar
        if (dx > 0) { 
            x += speed; 
            facingRight = true; 
        }
        if (dx < 0) { 
            x -= speed; 
            facingRight = false; 
        }

        // Deslocamento vertical (sensação de profundidade 2.5D na pista de caminhada)
        if (dy > 0) { 
            y += speed; 
        }
        if (dy < 0) { 
            y -= speed; 
        }

        // --------------------------------------------------------------------
        // LIMITES DO CENÁRIO (CLAMPING DE COORDENADAS)
        // --------------------------------------------------------------------
        if (x < 30) x = 30;
        if (x > GamePanel.WIDTH - 30) x = GamePanel.WIDTH - 30;
        if (y < GamePanel.GROUND_MIN_Y + 10) y = GamePanel.GROUND_MIN_Y + 10;
        if (y > GamePanel.GROUND_MAX_Y) y = GamePanel.GROUND_MAX_Y;
    }

    /**
     * Executa o ataque de Soco (Tecla J):
     * Possui um sistema de combo em 3 tempos:
     * - 1º toque: Jab rápido (15 de dano)
     * - 2º toque: Direto de punho cruzado (18 de dano)
     * - 3º toque: Soco pesado de avanço (25 de dano)
     */
    public void attackSoco() {
        if (attacking) return; // Não interrompe o golpe enquanto o frame atual não concluir
        
        attacking = true;
        hitboxActive = true;
        attackType = 1; // Soco

        activePunchCombo = punchCombo;
        if (punchCombo == 0) {
            attackDamage = 15;
            attackTimer = 14; // Animação rápida de 14 frames
        } else if (punchCombo == 1) {
            attackDamage = 18;
            attackTimer = 14;
        } else {
            attackDamage = 25;
            attackTimer = 18; // Golpe pesado de encerramento
        }

        // Avança na cadeia de combo e reseta a janela de tempo
        punchCombo = (punchCombo + 1) % 3;
        comboResetTimer = 40; // Dá ~0.66s para o jogador conectar o próximo golpe
    }

    /**
     * Executa o ataque de Chute (Tecla K):
     * Golpe com maior alcance e dano expressivo.
     * Reproduz as 3 fases completas: câmara de joelho -> chute alto -> chute frontal.
     */
    public void attackChute() {
        if (attacking) return;
        
        attacking = true;
        hitboxActive = true;
        attackType = 2; // Chute
        attackDamage = 25;
        attackTimer = 21; // 21 frames distribuídos harmonicamente entre as 3 poses
    }

    /**
     * Ativa o golpe supremo: FÚRIA DO DRAGÃO (Tecla L):
     * Consome 100% da barra de Fúria, concede invulnerabilidade temporária
     * e desfere uma investida rasgante com corte de plasma de 60 de dano.
     * 
     * @return true se o especial foi ativado com sucesso; false se a barra não estava cheia.
     */
    public boolean activateFuria() {
        if (furia >= 100 && !isFuriaActive) {
            furia = 0;             // Zera a barra de fúria
            isFuriaActive = true;  // Concede estado de super-armadura / invulnerabilidade
            furiaTimer = 180;      // Duração de 180 frames = 3 segundos
            attacking = true;      // Entra em pose de ataque devastador
            hitboxActive = true;   // Ativa área de impacto do especial
            attackType = 3;        // Identifica como o golpe supremo
            attackDamage = 60;     // Dano colossal
            attackTimer = 25;      // Animação de investida cortante
            return true;
        }
        return false;
    }

    /**
     * Adiciona pontos à barra de Fúria do Dragão (ganha ao acertar ou derrotar inimigos).
     * O valor máximo é limitado em 100.
     * 
     * @param amount Quantidade de pontos a acrescentar
     */
    public void addFuria(int amount) {
        this.furia += amount;
        if (this.furia > 100) {
            this.furia = 100;
        }
    }

    /**
     * Aplica dano ao jogador e ativa a animação de reação a golpe (Hurt).
     */
    @Override
    public void takeDamage(int damage) {
        if (isFuriaActive) return; // Totalmente invulnerável no Modo Fúria
        super.takeDamage(damage);
        if (alive) {
            hurtTimer = 14; // Reação a golpe por ~14 frames
        }
    }

    /**
     * Retorna a área de impacto do golpe calculada dinamicamente conforme
     * o alcance real do sprite (socos, chutes estendidos ou corte de fúria).
     */
    @Override
    public Rectangle getAttackBounds() {
        int attackWidth = 45; // Alcance do soco
        if (attackType == 2) attackWidth = 58; // Chute tem alcance estendido
        if (attackType == 3) attackWidth = 80; // Fúria do Dragão tem área de corte ampla

        int attackX = facingRight ? x + (width / 2) - 5 : x - (width / 2) - attackWidth + 5;
        return new Rectangle(attackX, y - height + 10, attackWidth, height - 10);
    }

    /**
     * Atualização dos temporizadores internos e ciclos de animação a cada frame.
     */
    @Override
    public void update() {
        // Reduz o temporizador do golpe atual
        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                attacking = false;
                hitboxActive = false;
            }
        }

        // Reduz o tempo de duração da Fúria do Dragão
        if (isFuriaActive) {
            furiaTimer--;
            if (furiaTimer <= 0) {
                isFuriaActive = false;
            }
        }

        // Janela de combo de socos
        if (comboResetTimer > 0) {
            comboResetTimer--;
            if (comboResetTimer <= 0) {
                punchCombo = 0; // Se demorar para golpear de novo, volta para o primeiro soco (Jab)
            }
        }

        // Temporizador de reação visual de dano
        if (hurtTimer > 0) {
            hurtTimer--;
        }

        // Atualização dos quadros de animação contínua (Idle e Walk)
        if (!attacking && hurtTimer <= 0) {
            if (isMoving && animWalk != null && animWalk.length > 0) {
                walkTimer++;
                if (walkTimer >= WALK_FRAME_DELAY) {
                    walkTimer = 0;
                    walkIndex = (walkIndex + 1) % animWalk.length;
                }
            } else if (animIdle != null && animIdle.length > 0) {
                idleTimer++;
                if (idleTimer >= IDLE_FRAME_DELAY) {
                    idleTimer = 0;
                    idleIndex = (idleIndex + 1) % animIdle.length;
                }
            }
        }
    }

    /**
     * Renderização gráfica do protagonista Thanatos Colt (Dragon of Night):
     * Desenha a sombra no solo, a aura de Fúria e o sprite animado correspondente
     * com espelhamento perfeito para esquerda/direita e ancoragem fixa nos pés.
     * 
     * @param g2 Contexto gráfico 2D
     */
    @Override
    public void render(Graphics2D g2) {
        // 1. Sombra do Personagem projetada no solo (pés firmes no chão 2.5D)
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x - 24, y - 8, 48, 15);

        // 2. Aura da Fúria do Dragão (Brilho dourado vibrante ao redor do corpo)
        if (isFuriaActive) {
            g2.setColor(new Color(255, 200, 0, 100));
            g2.fillOval(x - 32, y - 90, 64, 95);
        }

        // 3. Renderização do Sprite do Personagem
        if (spritesLoaded) {
            SpriteFrame frame = selectCurrentFrame();
            if (frame != null) {
                drawSpriteFrame(g2, frame, x, y, facingRight, SCALE);
            }

            // Efeito visual de plasma de corte durante o especial Fúria do Dragão
            if (attacking && attackType == 3) {
                g2.setColor(new Color(255, 120, 0, 200));
                int arcX = facingRight ? x + 10 : x - 90;
                g2.fillArc(arcX, y - 75, 80, 80, facingRight ? -50 : 130, 100);
            }
        } else {
            // Visual de fallback geométrico caso as imagens não estejam disponíveis
            renderFallback(g2);
        }
    }

    /**
     * Seleciona qual SpriteFrame deve ser exibido no instante exato da renderização,
     * respeitando as prioridades: Morte > Dano > Ataque > Caminhada > Idle.
     */
    private SpriteFrame selectCurrentFrame() {
        // Personagem derrotado
        if (!alive) {
            return animKnockdown;
        }

        // Reação a golpe recebido
        if (hurtTimer > 0 && animHurt != null && animHurt.length > 0) {
            int hurtIdx = (hurtTimer > 7) ? 0 : 1;
            return animHurt[hurtIdx % animHurt.length];
        }

        // Execução de Golpes
        if (attacking) {
            if (attackType == 1) { // Soco
                if (activePunchCombo == 0) {
                    return (attackTimer > 7) ? punch1Prep : punch1Strike;
                } else if (activePunchCombo == 1) {
                    return (attackTimer > 7) ? punch2Prep : punch2Strike;
                } else {
                    return punchHeavy;
                }
            } else if (attackType == 2) { // Chute
                if (attackTimer > 14) {
                    return kickChamber;
                } else if (attackTimer > 7) {
                    return kickHigh;
                } else {
                    return kickFront;
                }
            } else if (attackType == 3) { // Fúria do Dragão
                return kickSlide;
            }
        }

        // Movimentação (Caminhada)
        if (isMoving && animWalk != null && animWalk.length > 0) {
            return animWalk[walkIndex % animWalk.length];
        }

        // Postura padrão de descanso / combate (Idle)
        if (animIdle != null && animIdle.length > 0) {
            return animIdle[idleIndex % animIdle.length];
        }

        return null;
    }

    /**
     * Desenha o SpriteFrame aplicando a escala e o espelhamento horizontal (flip),
     * garantindo que o ponto de ancoragem nos pés coincida perfeitamente com (screenX, screenY).
     */
    private void drawSpriteFrame(Graphics2D g2, SpriteFrame frame, int screenX, int screenY, boolean right, double scale) {
        if (frame == null || frame.image == null) return;

        int drawW = (int) Math.round(frame.image.getWidth() * scale);
        int drawH = (int) Math.round(frame.image.getHeight() * scale);
        int offsetX = (int) Math.round(frame.anchorX * scale);
        int offsetY = (int) Math.round(frame.anchorY * scale);

        int drawY = screenY - offsetY;

        if (right) {
            // Olhando para a direita: renderização padrão
            int drawX = screenX - offsetX;
            g2.drawImage(frame.image, drawX, drawY, drawW, drawH, null);
        } else {
            // Olhando para a esquerda: espelha a imagem com largura negativa no eixo X
            int drawX = screenX - (drawW - offsetX);
            g2.drawImage(frame.image, drawX + drawW, drawY, -drawW, drawH, null);
        }
    }

    /**
     * Renderização procedural de emergência (caso a imagem não seja carregada).
     */
    private void renderFallback(Graphics2D g2) {
        int drawX = x - width / 2;
        int drawY = y - height;

        g2.setColor(new Color(30, 35, 45));
        g2.fillRect(drawX, drawY + 15, width, height - 15);

        g2.setColor(new Color(230, 190, 160));
        g2.fillRect(drawX + 8, drawY, 20, 18);

        g2.setColor(Color.RED);
        int eyeX = facingRight ? drawX + 22 : drawX + 10;
        g2.fillRect(eyeX, drawY + 6, 4, 4);

        if (attacking) {
            g2.setColor(isFuriaActive ? Color.YELLOW : Color.CYAN);
            int armX = facingRight ? drawX + width : drawX - 25;
            if (attackType == 1) {
                g2.fillRect(armX, drawY + 22, 25, 10);
            } else if (attackType == 2) {
                g2.fillRect(armX, drawY + 40, 30, 12);
            } else if (attackType == 3) {
                g2.setColor(new Color(255, 100, 0, 200));
                g2.fillArc(facingRight ? x : x - 70, drawY - 10, 70, height + 20, -60, 120);
            }
        }
    }

    // ------------------------------------------------------------------------
    // MÉTODOS DE CONSULTA (GETTERS ESPECÍFICOS)
    // ------------------------------------------------------------------------

    /** @return O valor acumulado da barra de fúria (0 a 100). */
    public int getFuria() { return furia; }

    /** @return true se a barra de fúria atingiu 100% e está pronta para uso. */
    public boolean isFuriaPronta() { return furia >= 100; }

    /** @return true se o jogador estiver imune a dano durante o modo Fúria. */
    public boolean isInvulnerable() { return isFuriaActive; }
}