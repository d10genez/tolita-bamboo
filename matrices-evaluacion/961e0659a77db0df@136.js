// https://observablehq.com/@jarrettmeyer/sankey-with-animated-gradients@136
export default function define(runtime, observer) {
  const main = runtime.module();
  const fileAttachments = new Map([["energy.csv",new URL("./d6774e9422bd72369f195a30d3a6b33ff9d41676cff4d89c93511e1a458efb3cfd16cbb7ce3fecdd8dd2466121e10c9bfe57fd73c7520bf358d352a92b898614",import.meta.url)]]);
  main.builtin("FileAttachment", runtime.fileAttachments(name => fileAttachments.get(name)));
  main.variable(observer()).define(["md"], function(md){return(
md`# Arquitectura de código abierto - Matrices de evaluación 

Cada categoría muestra sus relaciones `
)});
  main.variable(observer("svg")).define("svg", ["d3","DOM","width","height","graph","margin","format","sankey","arrow","duration"], function(d3,DOM,width,height,graph,margin,format,sankey,arrow,duration)
{
  const svg = d3.select(DOM.svg(width, height));
  
  const defs = svg.append("defs");
  
  // Add definitions for all of the linear gradients.
  const gradients = defs.selectAll("linearGradient")
    .data(graph.links)
    .join("linearGradient")
    .attr("id", d => d.gradient.id)
  gradients.append("stop").attr("offset", 0.0).attr("stop-color", d => d.source.color);
  gradients.append("stop").attr("offset", 1.0).attr("stop-color", d => d.target.color);
    
  // Add a g.view for holding the sankey diagram.
  const view = svg.append("g")
    .classed("view", true)
    .attr("transform", `translate(${margin}, ${margin})`);
  
  // Define the nodes.
  const nodes = view.selectAll("rect.node")
    .data(graph.nodes)
    .join("rect")
    .classed("node", true)
    .attr("id", d => `node-${d.index}`)
    .attr("x", d => d.x0)
    .attr("y", d => d.y0)
    .attr("width", d => d.x1 - d.x0)
    .attr("height", d => Math.max(1, d.y1 - d.y0))
    .attr("fill", d => d.color)
    .attr("opacity", 0.9);
  
  // Add titles for node hover effects.
  nodes.append("title").text(d => `${d.name}\n${format(d.value)}`);
  
  // Add text labels.
  view.selectAll("text.node")
    .data(graph.nodes)
    .join("text")
    .classed("node", true)
    .attr("x", d => d.x1)
    .attr("dx", 6)
    .attr("y", d => (d.y1 + d.y0) / 2)
    .attr("dy", "0.35em")
    .attr("fill", "black")
    .attr("text-anchor", "start")
    .attr("font-size", 10)
    .attr("font-family", "Arial, sans-serif")
    .text(d => d.name)
    .filter(d => d.x1 > width / 2)
    .attr("x", d => d.x0)
    .attr("dx", -6)
    .attr("text-anchor", "end");
  
  // Define the gray links.
  const links = view.selectAll("path.link")
    .data(graph.links)
    .join("path")
    .classed("link", true)
    .attr("d", sankey.sankeyLinkHorizontal())
    .attr("stroke", "black")
    .attr("stroke-opacity", 0.1)
    .attr("stroke-width", d => Math.max(1, d.width))
    .attr("fill", "none");
  
  // Add <title> hover effect on links.
  links.append("title").text(d => `${d.source.name} ${arrow} ${d.target.name}\n${format(d.value)}`);
    
  // Define the default dash behavior for colored gradients.
  function setDash(link) {
    let el = view.select(`#${link.path.id}`);
    let length = el.node().getTotalLength();
    el.attr("stroke-dasharray", `${length} ${length}`)
      .attr("stroke-dashoffset", length);
  }
  
  const gradientLinks = view.selectAll("path.gradient-link")
    .data(graph.links)
    .join("path")
    .classed("gradient-link", true)
    .attr("id", d => d.path.id)
    .attr("d", sankey.sankeyLinkHorizontal())
    .attr("stroke", d => d.gradient)
    .attr("stroke-opacity", 0)
    .attr("stroke-width", d => Math.max(1, d.width))
    .attr("fill", "none")
    .each(setDash);
  
  function branchAnimate(node) {
    let links = view.selectAll("path.gradient-link")
      .filter((link) => {
        return node.sourceLinks.indexOf(link) !== -1;
      });
    let nextNodes = [];
    links.each((link) => {
      nextNodes.push(link.target);
    });
    links.attr("stroke-opacity", 0.5)
      .transition()
      .duration(duration)
      .ease(d3.easeLinear)
      .attr("stroke-dashoffset", 0)
      .on("end", () => {
        nextNodes.forEach((node) => {
          branchAnimate(node);
        });
      });
  }
  
  function branchClear() {
    gradientLinks.transition();
    gradientLinks.attr("stroke-opactiy", 0)
      .each(setDash);
  }

  nodes.on("mouseover", branchAnimate)
    .on("mouseout", branchClear);
  
  return svg.node();
}
);
  main.variable(observer("graph")).define("graph", ["layout","energy","color","DOM"], function(layout,energy,color,DOM)
{
  const graph = layout(energy);
  
  graph.nodes.forEach((node) => {
    node.color = color(node.index / graph.nodes.length);
  });
  
  graph.links.forEach((link) => {
    link.gradient = DOM.uid("gradient");
    link.path = DOM.uid("path");
  });
  
  return graph;
}
);
  main.variable(observer("layout")).define("layout", ["sankey","size","nodePadding","nodeWidth"], function(sankey,size,nodePadding,nodeWidth){return(
sankey.sankey()
  .size(size)
  .nodePadding(nodePadding)
  .nodeWidth(nodeWidth)
)});
  main.variable(observer("format")).define("format", ["d3"], function(d3){return(
(value) => {
  let f = d3.format(",.0f");
  return " Criterios de Evaluación";
}
)});
  main.variable(observer("color")).define("color", ["d3"], function(d3){return(
(value) => {
  return d3.interpolateRainbow(value);
}
)});
  
  main.variable(observer("energy")).define("energy", function(){return(
{"nodes":[
{"name":"Funcional"}, //0
{"name":"Técnica"},   //1
{"name":"Organizativo"}, //2
{"name":"Legal"},   //3
{"name":"Político"}, //4
{"name":"Funcionalidad requerida"}, //5
{"name":"Evolución del producto"}, //6
{"name":"Gestión y administración de código fuente"}, //7
{"name":"Construcción de prototipos"},    //8
{"name":"CUMPLE"},            //9
{"name":"Listado de funcionalidades"}, //10
{"name":"Servidor"}, //11
{"name":"Repositorio"}, //12
{"name":"Gestión de versiones"}, //13
{"name":"Wiki"},  //14
{"name":"Foros"},   //15
{"name":"Plataformas de destino compatibles"},  //16
{"name":"Fiabilidad y mantenimiento"},  //17
{"name":"Compatibilidad en sistemas operativos"}, //18
{"name":"Compatibilidad entre formatos"}, //19
{"name":"Métricas del código fuente"},  //20
{"name":"PARCIALMENTE"},  //21
{"name":"Número abierto de adaptaciones requeridas"}, //22
{"name":"Frecuencia de cambios"}, //23
{"name":"Dependencia de otros software"}, //24
{"name":"DOS"}, //25
{"name":"REPORTES"}, //26
{"name":"La comunidad existe"}, //27
{"name":"Evolución del producto"}, //28
{"name":"Soporte disponible"}, //29
{"name":"Tráfico del proyecto"},  //30
{"name":"Colaboradores/Usuarios"}, //31
{"name":"Número de colaboradores"}, //32
{"name":"Número de usuarios"},  //33
{"name":"Número de visitas"},  //34
{"name":"25/MES"},  //35
{"name":"Sin efecto copyleft para complementos o combinaciones"}, //36
{"name":"Sin responsabilidad por el código de terceros"},  // 37
{"name":"Sin infracciones de patentes"},  //38
{"name":"Creative commons: parametrización"},  //39
{"name":"Garantías del correcto funcionamiento"},  //40
{"name":"Se restringe el uso de patentes en obras derivadas"},  //41
{"name":"Efectos de las licencias"},  //42
{"name":"Responsabilidad en la reproducción"},  // 43
{"name":"¿Se puede comercializar obras derivadas?"},  // 44
{"name":"¿Hay responsabilidad sobre las reproducciones?"},  // 45
{"name":"Influir en el desarrollo ulterior con respecto a las necesidades individuales"},  //46
{"name":"Publicidad, efectos de marketing a través del uso y participación"}, //47
{"name":"El uso del proyecto promueve publicidad orgánica"}, // 48
{"name":"¿En cuantos lenguajes se ha documentado?"}, //49
{"name":" ¿Cuál es la participación y la discusión en el foro?"}, //50
{"name":"UNO"}
],
"links":[
{"source":0,"target":5,"value":280.322},
{"source":0,"target":6,"value":280.322},
{"source":0,"target":10,"value":280.320},
{"source":10,"target":11,"value":180.322},
{"source":10,"target":12,"value":180.322},
{"source":10,"target":13,"value":180.322},
{"source":10,"target":14,"value":180.322},
{"source":10,"target":15,"value":180.322},
{"source":5,"target":7,"value":180.322},
{"source":6,"target":8,"value":180.322},
{"source":7,"target":9,"value":75.571},
{"source":8,"target":9,"value":75.571},
{"source":1,"target":16,"value":280.322},
{"source":1,"target":17,"value":280.322},
{"source":1,"target":22,"value":280.322},
{"source":1,"target":20,"value":280.322},
{"source":1,"target":23,"value":280.322},
{"source":1,"target":24,"value":280.322},
{"source":2,"target":27,"value":280.322},
{"source":2,"target":28,"value":280.322},
{"source":2,"target":29,"value":280.322},
{"source":2,"target":32,"value":280.322},
{"source":2,"target":33,"value":280.322},
{"source":2,"target":34,"value":280.322},
{"source":3,"target":36,"value":280.322},
{"source":3,"target":37,"value":280.322},
{"source":3,"target":38,"value":280.322},
{"source":3,"target":42,"value":280.322},
{"source":3,"target":43,"value":280.322},
{"source":4,"target":46,"value":280.322},
{"source":4,"target":47,"value":280.322},
{"source":4,"target":49,"value":280.322},
{"source":4,"target":50,"value":280.322},
{"source":16,"target":18,"value":180.322},
{"source":16,"target":19,"value":180.322},
{"source":17,"target":20,"value":180.322},
{"source":19,"target":21,"value":75.000},
{"source":18,"target":9,"value":75.000},
{"source":20,"target":9,"value":75.000},
{"source":22,"target":25,"value":75.000},
{"source":20,"target":26,"value":75.000},
{"source":23,"target":26,"value":75.000},
{"source":24,"target":26,"value":75.000},
{"source":27,"target":30,"value":180.000},
{"source":28,"target":20,"value":180.000},
{"source":29,"target":31,"value":180.000},
{"source":30,"target":9,"value":75.000},
{"source":20,"target":9,"value":75.000},
{"source":31,"target":9,"value":75.000},
{"source":32,"target":25,"value":75.000},
{"source":33,"target":35,"value":75.000},
{"source":34,"target":25,"value":75.000},
{"source":36,"target":39,"value":180.000},
{"source":37,"target":40,"value":180.000},
{"source":38,"target":41,"value":180.000},
{"source":39,"target":9,"value":75.000},
{"source":40,"target":9,"value":75.000},
{"source":41,"target":21,"value":75.000},
{"source":42,"target":44,"value":180.000},
{"source":43,"target":45,"value":180.000},
{"source":44,"target":9,"value":75.000},
{"source":45,"target":9,"value":75.000},
{"source":46,"target":22,"value":180.000},
{"source":47,"target":48,"value":180.000},
{"source":22,"target":9,"value":75.000},
{"source":48,"target":9,"value":75.000},
{"source":49,"target":51,"value":75.000},
{"source":50,"target":21,"value":19.013}
]}
)});
  main.variable(observer("size")).define("size", ["width","margin","height"], function(width,margin,height){return(
[width - 2 * margin, height - 2 * margin]
)});
  main.variable(observer("height")).define("height", function(){return(
600
)});
  main.variable(observer("margin")).define("margin", function(){return(
10
)});
  main.variable(observer("nodeWidth")).define("nodeWidth", function(){return(
20
)});
  main.variable(observer("nodePadding")).define("nodePadding", function(){return(
10
)});
  main.variable(observer("duration")).define("duration", function(){return(
250
)});
  main.variable(observer("arrow")).define("arrow", function(){return(
"\u2192"
)});
  main.variable(observer("d3")).define("d3", ["require"], function(require){return(
require("d3@5.9")
)});
  main.variable(observer("sankey")).define("sankey", ["require"], function(require){return(
require("d3-sankey@0.12")
)});
  return main;
}

