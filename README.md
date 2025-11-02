Piscina Fácil:
Gerenciador de Tarefas Semanais (Android Nativo com Kotlin)
Piscina Fácil é um aplicativo Android nativo, desenvolvido em Kotlin,
projetado para ajudar técnicos de manutenção de piscinas a organizar e gerenciar suas tarefas diárias e semanais.
O app combina uma interface de usuário intuitiva com funcionalidades inteligentes para otimizar a rotina de trabalho.Principais Funcionalidades1. 
Interface Dinâmica e Intuitiva•Saudação Personalizada: A tela inicial recebe o usuário com uma saudação que muda de acordo com a hora do dia ("Bom Dia", "Boa Tarde", etc.),
criando uma experiência mais acolhedora.•Destaque do Dia Atual:
O botão correspondente ao dia da semana atual é destacado visualmente, 
permitindo que o usuário se localize rapidamente.•Identidade Visual Única: 
O aplicativo conta com um ícone personalizado e imagens de fundo em tela cheia, 
tanto na tela inicial quanto na de tarefas, proporcionando uma experiência imersiva e profissional.2.
Gerenciamento Completo de Tarefas (CRUD)•Criação Detalhada: O usuário pode adicionar novas tarefas, 
especificando não apenas a descrição, mas também uma localização e um horário de prioridade.•Edição Simplificada:
Um botão de edição permite alterar qualquer informação de uma tarefa (descrição, localização ou horário)
através de uma caixa de diálogo que já vem preenchida com os dados atuais.
•Marcação de Conclusão: As tarefas podem ser marcadas como concluídas com um simples toque, 
alterando seu estado visual para fácil identificação.•Exclusão Rápida: 
Tarefas podem ser removidas da lista com um clique longo.3.
Recursos Inteligentes de Priorização e Navegação•Ordenação por Horário:
As tarefas que possuem um horário definido são automaticamente movidas para o topo da lista,
ordenadas da mais cedo para a mais tarde, garantindo que as prioridades do dia fiquem sempre visíveis.•Integração com Mapas: 
Tarefas que incluem uma localização exibem um botão "Ir", que abre o endereço diretamente em um aplicativo de mapa instalado no celular 
(Google Maps, Waze, etc.).4. Persistência de Dados e Ciclo de Vida•Salvamento Local: Utilizando SharedPreferences e Gson, 
todas as tarefas são salvas localmente no dispositivo. O trabalho do usuário nunca é perdido, 
mesmo que o aplicativo seja fechado.•Reinício Semanal Automático: Aos domingos,
o aplicativo executa uma lógica inteligente que desmarca todas as tarefas da semana, 
preparando a lista para um novo ciclo de trabalho e notificando o usuário da ação através de um Toast.
Qualidade de Código e Boas PráticasO desenvolvimento deste aplicativo seguiu as práticas modernas recomendadas pelo Google:
•Gerenciamento de Dependências: Todas as dependências do projeto foram migradas para o Gradle Version Catalog (libs.versions.toml), 
centralizando o controle de versões e tornando o build mais limpo e seguro.•Kotlin KTX:
Foram utilizadas as funções de extensão (KTX) para escrever um código mais conciso e idiomático, 
como na manipulação de SharedPreferences e na conversão de cores.•Recursos de String: 
Textos dinâmicos foram movidos para o arquivo strings.xml, 
facilitando a manutenção e preparando o app para futuras traduções.
