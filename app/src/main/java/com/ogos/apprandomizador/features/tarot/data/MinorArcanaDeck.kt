package com.ogos.apprandomizador.features.tarot.data

import com.ogos.apprandomizador.features.tarot.domain.CardMeaning
import com.ogos.apprandomizador.features.tarot.domain.DeckProvider
import com.ogos.apprandomizador.features.tarot.domain.Suit
import com.ogos.apprandomizador.features.tarot.domain.TarotCard

/**
 * Conteúdo autoral em PT-BR para os 56 Arcanos Menores (Paus w, Copas c, Espadas s, Ouros p).
 * Cada orientação tem exatamente três palavras-chave, separadas por ", ".
 * Textos sem afirmações médicas, financeiras ou de gênero: quando pertinente, sugerem apoio profissional.
 */
object MinorArcanaDeck : DeckProvider {
    private data class Entry(
        val rank: Int,
        val name: String,
        val up: String,
        val down: String,
        val upText: String,
        val downText: String
    )

    private val wands = listOf(
        Entry(1, "Ás de Paus", "começo, energia criativa, inspiração", "atraso, falta de energia, momento errado",
            "Um novo projeto ou uma onda de paixão aparece como convite. Aproveite a inspiração para dar o primeiro passo com entusiasmo.",
            "A energia para começar pode estar travada ou a ideia pode ter surgido antes da hora. Reavalie o momento antes de avançar."),
        Entry(2, "Dois de Paus", "planejamento, reflexão, visão de futuro", "medo de agir, cautela excessiva, espera",
            "Você está diante de uma encruzilhada e pode medir suas forças antes de decidir. Um tempo de reflexão prepara a próxima ação.",
            "A cautela pode estar virando paralisia. Escolha um primeiro passo pequeno para sair da espera."),
        Entry(3, "Três de Paus", "expansão, cooperação, confiança", "atrasos, parcerias frágeis, dificuldade de delegar",
            "Uma base sólida já está sendo construída e os primeiros resultados começam a aparecer. Continue ampliando horizontes com colaboração.",
            "Algumas parcerias ou prazos podem estar travando o progresso. Revise combinados e aceite apoio quando precisar."),
        Entry(4, "Quatro de Paus", "celebração, estabilidade, lar", "instabilidade em casa, descanso adiado, celebração frustrada",
            "Uma etapa importante chega a um bom termo e pede celebração e descanso. Aproveite a sensação de segurança e pertencimento.",
            "O descanso ou a estabilidade podem estar ameaçados. Procure equilibrar as obrigações com momentos de cuidado com você e com o lar."),
        Entry(5, "Cinco de Paus", "conflito, competição, debate de ideias", "evitar confronto, rendição, caos sem propósito",
            "Há divergências e tensões que podem ser usadas como teste de força e de ideias. Ouça as críticas e separe o debate útil da disputa.",
            "Evitar o confronto pode estar deixando um problema crescer. Procure um diálogo claro, sem se render ou se dispersar."),
        Entry(6, "Seis de Paus", "reconhecimento, liderança, vitória", "vaidade, popularidade instável, queda",
            "Seus esforços começam a ser reconhecidos e você pode assumir uma posição de liderança. Celebre a conquista com humildade.",
            "A busca por aprovação pode estar virando vaidade ou dependência de opiniões. Volte a medir o sucesso pelos seus próprios critérios."),
        Entry(7, "Sete de Paus", "defesa de posição, firmeza, coragem", "cansaço de luta, sensação de derrota, falta de recursos",
            "Você precisa sustentar suas convicções diante de pressões externas. Mesmo em minoria, você tem condições de manter seu terreno.",
            "A luta constante pode estar esgotando suas forças. Avalie o que vale defender e onde pode buscar apoio ou descanso."),
        Entry(8, "Oito de Paus", "velocidade, notícias, movimento rápido", "atrasos, planos cancelados, informação tardia",
            "Os acontecimentos se aceleram e mensagens ou decisões podem chegar rapidamente. Mantenha a clareza para acompanhar o ritmo.",
            "Planos podem travar ou a informação pode chegar fora de hora. Desacelere um pouco e confirme os detalhes antes de agir."),
        Entry(9, "Nove de Paus", "vigilância, resiliência, proteção de limites", "desconfiança excessiva, paranoia, dificuldade de confiar",
            "Você já passou por dificuldades e agora mantém atenção aos próprios limites. Aja com cautela, sem deixar a vigilância virar isolamento.",
            "A desconfiança pode estar pesando mais do que a situação exige. Observe fatos concretos antes de concluir intenções alheias."),
        Entry(10, "Dez de Paus", "sobrecarga, responsabilidade, esforço prolongado", "alívio de peso, delegação, recusa de excessos",
            "Você assumiu muitas responsabilidades e o esforço cobra seu preço. Reveja prioridades e veja o que pode ser compartilhado.",
            "Chegou a hora de soltar parte da carga. Delegue, recuse o que não cabe e recupere o equilíbrio."),
        Entry(11, "Valete de Paus", "mensagem, curiosidade, impulso criativo", "promessas vazias, notícias atrasadas, entusiasmo sem continuidade",
            "Uma notícia ou proposta pode trazer um novo começo de aprendizado. Mantenha a curiosidade e a disposição para experimentar.",
            "Um entusiasmo inicial pode não se sustentar. Verifique se as promessas são concretas antes de se comprometer."),
        Entry(12, "Cavaleiro de Paus", "ação rápida, energia, mudança", "imprudência, impulsividade, conflitos por pressa",
            "Há energia para agir com rapidez, e isso pode trazer viagens, mudanças ou novas conexões. Canalize o impulso com direção.",
            "A pressa pode gerar decisões impulsivas ou desgaste nas relações. Desacelere o suficiente para escolher o caminho certo."),
        Entry(13, "Rainha de Paus", "confiança, independência, magnetismo", "exigência excessiva, ciúme, insegurança",
            "Você sabe o que quer e pode conduzir sua vida com otimismo e autonomia. Use essa presença para inspirar quem está perto.",
            "A cobrança por perfeição ou o medo de perder espaço podem afetar a confiança. Lembre do que já conquistou."),
        Entry(14, "Rei de Paus", "liderança, visão, determinação", "autoritarismo, impaciência, abuso de poder",
            "Você pode liderar projetos grandes com visão e determinação. Assuma a autoridade com responsabilidade e abertura para ouvir.",
            "A firmeza pode estar virando imposição. Reveja como usa sua autoridade e abra espaço para outras opiniões.")
    )

    private val cups = listOf(
        Entry(1, "Ás de Copas", "amor, alegria sincera, inspiração", "emoções reprimidas, vazio interior, criatividade parada",
            "Um novo afeto, uma alegria genuína ou uma inspiração criativa se abre diante de você. Permita-se sentir sem pressa.",
            "Emoções podem estar contidas e a criatividade adormecida. Dê nome ao que sente para liberar espaço."),
        Entry(2, "Dois de Copas", "união, parceria, entendimento mútuo", "desequilíbrio, acordos quebrados, afeto não correspondido",
            "Uma conexão baseada em respeito e entendimento mútuo pode se fortalecer. Cultive a troca honesta.",
            "Pode haver desequilíbrio ou ruído em um vínculo. Converse com clareza sobre expectativas e limites."),
        Entry(3, "Três de Copas", "celebração, amizade, apoio", "excesso de festa, amizades superficiais, isolamento",
            "Momentos de alegria com amigos ou família trazem apoio e reconhecimento. Celebre o que foi conquistado com gratidão.",
            "A busca por distração pode estar mascarando um vazio. Procure vínculos mais profundos e momentos de presença real."),
        Entry(4, "Quatro de Copas", "apatia, introspecção, desânimo", "saída do desânimo, nova motivação, oportunidades percebidas",
            "Um período de desinteresse pode estar fazendo você deixar de notar novas possibilidades. Olhe ao redor com calma.",
            "Você começa a enxergar oportunidades que estavam fora de foco. Permita-se retomar o interesse aos poucos."),
        Entry(5, "Cinco de Copas", "decepção, arrependimento, luto", "aceitação, seguir em frente, soltar a dor",
            "Uma perda ou decepção pede tempo para ser reconhecida. Lembre que ainda há recursos e caminhos disponíveis.",
            "Você começa a aceitar o que passou e a seguir adiante. Solte a dor no seu ritmo."),
        Entry(6, "Seis de Copas", "lembranças, afeto, reencontros", "nostalgia excessiva, passado doloroso, dificuldade de viver o presente",
            "Reencontros e lembranças da infância trazem aconchego e leveza. Aproveite o carinho que vem do passado.",
            "Olhar demais para trás pode impedir que você viva o agora. Honre a memória sem se prender a ela."),
        Entry(7, "Sete de Copas", "muitas opções, fantasia, tentação", "ilusões desfeitas, visão realista, clareza",
            "Há muitas possibilidades, mas nem todas são reais. Separe fantasias de caminhos concretos antes de escolher.",
            "Ilusões começam a se dissipar e você ganha clareza. Avalie a situação com os pés no chão."),
        Entry(8, "Oito de Copas", "deixar para trás, busca interior, transição", "dificuldade de soltar, medo da solidão, satisfação superficial",
            "Algo que já teve valor pede que você siga em busca de um sentido mais profundo. Saia com coragem, sem pressa para ter todas as respostas.",
            "Apegar-se ao que passou pode impedir uma busca verdadeira. Reconheça o medo e avalie o que realmente quer."),
        Entry(9, "Nove de Copas", "contentamento, realização, orgulho", "insaciedade, busca de prazer sem sentido, materialismo",
            "Desejos importantes começam a se realizar e você pode sentir orgulho do caminho. Aprecie o bem-estar que chega.",
            "Ter tudo ainda deixa um vazio se o sentido não estiver presente. Pergunte-se o que realmente nutre você."),
        Entry(10, "Dez de Copas", "harmonia familiar, segurança, união", "tensão em casa, quebra de harmonia, perda de conforto",
            "Laços familiares e afetivos transmitem segurança e união. Valorize a convivência e o cuidado mútuo.",
            "A harmonia em casa pode estar abalada. Procure diálogo e acolhimento antes de tomar decisões."),
        Entry(11, "Valete de Copas", "proposta afetiva, sensibilidade, impulso criativo", "manipulação disfarçada, imaturidade, instabilidade emocional",
            "Uma mensagem afetuosa ou um impulso criativo puro pode trazer novidades. Acolha a sensibilidade com cuidado.",
            "Emoções instáveis ou promessas pouco sinceras podem confundir. Observe atitudes além das palavras."),
        Entry(12, "Cavaleiro de Copas", "romance, gesto sincero, convite", "idealização, decepção, sonhos não realizados",
            "Um gesto romântico ou um convite sincero pode chegar até você. Siga o coração com atenção ao equilíbrio.",
            "Ideias muito idealizadas podem levar a decepções. Ajuste expectativas à realidade do vínculo."),
        Entry(13, "Rainha de Copas", "empatia, intuição, acolhimento", "superproteção, dependência emocional, pensamento confuso",
            "Sua sensibilidade e intuição podem ajudar a acolher quem está ao redor. Cuide de si para manter o equilíbrio.",
            "Emoções intensas podem dificultar uma visão clara. Separe o que sente do que de fato está acontecendo."),
        Entry(14, "Rei de Copas", "maturidade emocional, diplomacia, equilíbrio", "instabilidade emocional, manipulação, dependência",
            "Você pode lidar com as emoções com equilíbrio e oferecer apoio sensato. Use sua diplomacia para acalmar tensões.",
            "Emoções difíceis de controlar podem influenciar decisões. Busque equilíbrio antes de se posicionar.")
    )

    private val swords = listOf(
        Entry(1, "Ás de Espadas", "clareza, decisão, verdade", "crueldade, decisão injusta, pensamento confuso",
            "Clareza mental pode trazer uma decisão difícil, porém acertada. Corte dúvidas e confie no que a razão já revelou.",
            "Uma decisão tomada com dureza ou pressa pode ser injusta. Antes de concluir, pese os fatos e o impacto sobre os outros."),
        Entry(2, "Dois de Espadas", "impasse, escolha sob incerteza, bloqueio emocional", "saída do impasse, disposição para a verdade, revelação",
            "Você pode estar evitando olhar para um problema e precisa decidir mesmo sem todas as informações. Reúna o que é possível saber.",
            "Está surgindo disposição para encarar a verdade e sair do impasse. Permita-se ver o que antes estava encoberto."),
        Entry(3, "Três de Espadas", "tristeza, mágoa, decepção", "aceitação da dor, início da cura, superação do luto",
            "Uma decepção amorosa ou de amizade pode ferir fundo. Reconheça a dor como parte de um processo que traz verdade e alívio aos poucos.",
            "A dor começa a ser aceita e a cura a aparecer. Dê a si mesmo o tempo necessário para se recuperar."),
        Entry(4, "Quatro de Espadas", "pausa, descanso, recuperação", "dificuldade de descansar, excesso de atividade, esgotamento",
            "Um período de pausa, silêncio ou meditação ajuda a recuperar a força mental. Cuide do descanso sem culpa.",
            "Você pode estar sem descanso e perto do esgotamento. Reduza o ritmo e observe os sinais de cansaço."),
        Entry(5, "Cinco de Espadas", "conflito difícil, vitória com custo, desgaste de relações", "saída de conflito, reconciliação, disputa sem sentido",
            "Um conflito pode trazer vantagem, mas o preço pode ser alto, como o desgaste de relações ou da reputação. Avalie o que vale a pena manter.",
            "Abrir mão de uma disputa destrutiva pode abrir espaço para reconciliação. Escolha as batalhas que realmente importam."),
        Entry(6, "Seis de Espadas", "transição, travessia, dias mais leves", "dificuldade de sair de uma situação, repetição de erros, bagagem do passado",
            "Você caminha em direção a um tempo mais tranquilo, com mudanças de rota ou de lugar. Siga com calma e confiança.",
            "Sair de uma situação difícil pode estar sendo mais complicado do que o esperado. Observe quais padrões se repetem."),
        Entry(7, "Sete de Espadas", "estratégia, desvio, discrição", "verdade revelada, reparação, honestidade",
            "Algum caminho indireto pode ser necessário agora. Aja com discrição, mas sem abrir mão da transparência com quem importa.",
            "Uma verdade pode vir à tona, ou você pode decidir abandonar atalhos desonestos. Escolha a honestidade, mesmo que seja desconfortável."),
        Entry(8, "Oito de Espadas", "limitação percebida, medo de agir, pensamentos que aprisionam", "consciência da própria força, libertação, saída de armadilhas",
            "Muitas amarras podem existir mais na mente do que na realidade. Questione o medo que paralisa e observe quais limites são reais.",
            "Você começa a perceber a própria força e a se libertar de limitações internas. Dê o próximo passo com cuidado."),
        Entry(9, "Nove de Espadas", "ansiedade, preocupação intensa, culpa", "superação gradual da ansiedade, apoio profissional, alívio",
            "Preocupações e culpas podem estar amplificadas pela imaginação. Procure separar temores de fatos concretos e, se persistir, converse com alguém de confiança.",
            "A ansiedade começa a diminuir aos poucos. Buscar apoio, inclusive profissional, pode ajudar nesse processo."),
        Entry(10, "Dez de Espadas", "fim doloroso, conclusão, fechamento", "aceitação do fim inevitável, início da recuperação, saída da crise",
            "Uma fase difícil chega ao fim. Esse encerramento dolorido pode abrir espaço para um recomeço. Permita-se fechar o ciclo.",
            "Você aceita o que não pode mudar e começa a se recuperar. Pequenos passos já são sinais de recomeço."),
        Entry(11, "Valete de Espadas", "curiosidade, observação, vigilância", "crítica dura, pensamento superficial, rumores",
            "Vontade de aprender e de entender os fatos pode trazer ideias novas. Observe com atenção, mas sem julgar antes da hora.",
            "Críticas duras ou informações sem checagem podem circular. Verifique a fonte antes de repetir ou acreditar."),
        Entry(12, "Cavaleiro de Espadas", "ação rápida, franqueza, mudança abrupta", "agressividade, crítica destrutiva, impulsividade",
            "Há energia para agir com rapidez e franqueza, o que pode gerar mudanças importantes. Cuide para que a firmeza não vire aspereza.",
            "Uma postura muito dura ou impulsiva pode ferir pessoas e decisões. Respire antes de responder."),
        Entry(13, "Rainha de Espadas", "clareza, independência, senso crítico", "frieza, ressentimento, crítica excessiva",
            "Você tem visão clara, senso crítico e independência para decidir. Use a franqueza com cuidado para que ela construa.",
            "A racionalidade pode virar distanciamento ou dureza com os outros. Lembre-se de que o acolhimento também faz parte da clareza."),
        Entry(14, "Rei de Espadas", "lógica, autoridade, imparcialidade", "rigidez, autoritarismo, manipulação pelo conhecimento",
            "Decisões guiadas pela razão e pela imparcialidade podem trazer ordem. Mantenha a autoridade aliada à justiça e à escuta.",
            "A racionalidade pode se tornar rigidez ou uso do conhecimento para controlar outros. Reveja a forma como decide.")
    )

    private val pentacles = listOf(
        Entry(1, "Ás de Ouros", "oportunidade, abundância, novo recurso", "oportunidade perdida, desperdício de recursos, materialismo",
            "Pode surgir uma oportunidade concreta de crescimento material ou de estabilidade. Avalie com calma e aproveite com responsabilidade.",
            "Uma chance pode estar passando sem ser percebida, ou o foco excessivo em bens materiais pode esvaziar outras prioridades. Reavalie o que realmente importa."),
        Entry(2, "Dois de Ouros", "equilíbrio, adaptação, flexibilidade", "desorganização, dificuldade de administrar, estresse",
            "Você precisa equilibrar duas demandas ou compromissos. Mantenha a leveza e a disposição para se adaptar.",
            "A falta de organização pode gerar estresse nas obrigações. Organize prioridades e faça um plano simples."),
        Entry(3, "Três de Ouros", "trabalho em equipe, reconhecimento, qualidade", "problemas em equipe, falta de reconhecimento, trabalho sem cuidado",
            "Um trabalho bem feito em grupo pode ser reconhecido e trazer frutos. Valorize a colaboração e o capricho nos detalhes.",
            "Ruídos na equipe ou um trabalho apressado podem atrapalhar o resultado. Revise os combinados e a qualidade entregue."),
        Entry(4, "Quatro de Ouros", "segurança, cautela, preservação", "desapego, abertura a novidades, generosidade",
            "Você busca segurança e preserva o que conquistou. Cuidar dos recursos é válido, desde que não impeça o crescimento.",
            "Pode ser o momento de se abrir para novas trocas com equilíbrio. Avalie cada passo antes de gastar ou investir por impulso."),
        Entry(5, "Cinco de Ouros", "dificuldade, solidão, necessidade de ajuda", "superação de dificuldades, acolhimento, apoio encontrado",
            "Momentos de escassez ou de isolamento podem aparecer. Pedir ajuda é um passo de força, não de fraqueza.",
            "Você começa a sair de uma fase difícil e encontra apoio. Reconheça as pessoas e recursos que estão ao seu lado."),
        Entry(6, "Seis de Ouros", "generosidade, apoio mútuo, partilha", "desigualdade, desequilíbrio na partilha, ajuda mal direcionada",
            "Ajudar e ser ajudado pode fortalecer vínculos. Partilhe recursos com equilíbrio e reciprocidade.",
            "A partilha pode estar desigual ou a ajuda pode não estar sendo bem direcionada. Revise o que é justo para todos."),
        Entry(7, "Sete de Ouros", "paciência, espera, crescimento lento", "impaciência, desistência precoce, insatisfação com resultados",
            "O resultado ainda amadurece devagar. Tenha paciência e continue trabalhando para o futuro.",
            "A pressa pode levar a abandonar um esforço antes da hora. Avalie se o caminho ainda faz sentido."),
        Entry(8, "Oito de Ouros", "dedicação, trabalho constante, aprimoramento", "perfeccionismo, rotina pesada, falta de progresso",
            "O esforço diário e o aperfeiçoamento de habilidades constroem resultados. Dê um passo de cada vez.",
            "A busca por perfeição ou uma rotina desgastante pode travar o progresso. Ajuste expectativas e celebre pequenas conquistas."),
        Entry(9, "Nove de Ouros", "autonomia, conforto, apreciação da vida", "acomodação, dependência de bens materiais, perda de conquistas",
            "Você pode sentir autonomia e prazer em usufruir do que construiu. Aprecie esse momento com gratidão.",
            "A acomodação pode fazer você perder o impulso ou depender demais do que tem. Mantenha o equilíbrio."),
        Entry(10, "Dez de Ouros", "estabilidade familiar, tradição, apoio de parentes", "conflitos familiares, dependência excessiva, disputas por herança",
            "Laços familiares e tradições trazem estabilidade e apoio. Valorize esse alicerce e o cuidado entre gerações.",
            "Tensões familiares podem afetar decisões importantes. Converse abertamente e cuide da autonomia de cada um."),
        Entry(11, "Valete de Ouros", "aprendizado, início, primeiros ganhos", "irresponsabilidade, evitar aprender, planos irreais",
            "Um começo modesto, uma nova etapa de estudo ou uma proposta prática podem abrir caminhos. Construa a base com paciência.",
            "Planos muito otimistas ou o hábito de evitar aprender podem atrapalhar. Ajuste metas para o que é realista."),
        Entry(12, "Cavaleiro de Ouros", "confiabilidade, pragmatismo, trabalho constante", "excesso de cautela, acomodação, medo de arriscar",
            "Um movimento lento, porém constante, leva a um objetivo com segurança. Valorize a disciplina e a praticidade.",
            "A cautela excessiva pode se transformar em estagnação. Avalie se há algum risco calculado que vale a pena assumir."),
        Entry(13, "Rainha de Ouros", "acolhimento, praticidade, generosidade", "superproteção, controle excessivo, dificuldade de estabelecer limites",
            "Você sabe criar conforto e cuidar do ambiente com praticidade e generosidade. Mantenha o cuidado com você mesmo também.",
            "A preocupação excessiva com o conforto pode virar controle ou dificuldade de dizer não. Estabeleça limites claros."),
        Entry(14, "Rei de Ouros", "experiência, solidez, responsabilidade", "ganância, uso de recursos para manipular, rigidez",
            "Experiência e visão prática podem sustentar projetos de longo prazo. Use sua influência com responsabilidade.",
            "A busca excessiva por ganhos pode gerar distorções. Revise como os recursos são usados e quem se beneficia.")
    )

    private val cards: List<TarotCard> =
        build(Suit.WANDS, "w", wands) +
            build(Suit.CUPS, "c", cups) +
            build(Suit.SWORDS, "s", swords) +
            build(Suit.PENTACLES, "p", pentacles)

    private fun build(suit: Suit, prefix: String, entries: List<Entry>): List<TarotCard> =
        entries.map { entry ->
            TarotCard(
                id = prefix + entry.rank.toString().padStart(2, '0'),
                number = entry.rank,
                name = entry.name,
                upright = CardMeaning(entry.up.split(", "), entry.upText),
                reversed = CardMeaning(entry.down.split(", "), entry.downText),
                suit = suit
            )
        }

    override fun getDeck(): List<TarotCard> = cards
}
