<#-- template to create the name of the runner script -->
<#if osName?upper_case?contains("WIN")>
bundlemonkey.cmd
<#else>
bundlemonkey.sh
</#if>