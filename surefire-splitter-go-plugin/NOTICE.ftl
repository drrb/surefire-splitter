<#--

    surefire-splitter-go-plugin
    Copyright (C) 2015 drrb

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with surefire-splitter-go-plugin. If not, see <http://www.gnu.org/licenses />.

-->
Surefire Splitter
Copyright (C) 2015 drrb

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Surefire Splitter. If not, see <http://www.gnu.org/licenses/>.

This project includes:
<#list dependencyMap as entry>
    <#assign project = entry.getKey()/>
    <#assign licenses = entry.getValue()/>
  - ${project.name} (${project.groupId}:${project.artifactId}:${project.version} - ${project.url!"no url defined"}) under ${licenses[0]}
</#list>
