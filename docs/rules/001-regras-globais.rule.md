# Regras a serem seguidas antes de qualquer desenvolvimento

* **NÃO** alterar arquivos no pacote "configs"
* **NÃO** adicione novas bibliotecas no `pom.xml`. Utilize apenas o que já está disponível
* O nome da branch segue o seguinte padrão [tipo_commit]/[numero_issue], quando for fazer commit coloque a mensagem da seguinte forma "[tipo_commit]:[mensagem]"
* Sempre ajustar os imports para não ficar de forma estensiva, não deixar assim `br.com.wferreiracosta.louis.models.enums.UserType.COMMON`
* Executar `mvn clean test` após cada etapa para garantir que nenhuma regressão foi introduzida.