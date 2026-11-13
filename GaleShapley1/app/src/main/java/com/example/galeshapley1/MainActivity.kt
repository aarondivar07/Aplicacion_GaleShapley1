
package com.example.galeshapley1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.ArrayDeque

// --------------------------------------------------
// PALETA DE COLOREs

val AzulMarino = Color(0xFF102A56)
val AzulPrincipal = Color(0xFF2875D0)
val AzulClaro = Color(0xFFDCEBFF)
val Fondo = Color(0xFFF3F7FD)
val Blanco = Color(0xFFFFFFFF)
val Verde = Color(0xFF16865C)
val Rojo = Color(0xFFD64F5F)
val Morado = Color(0xFF8960C5)
val GrisTexto = Color(0xFF65758B)

// --------------------------------------------------
// MODELOS DE DATOS
// --------------------------------------------------

data class Evento(
    val descripcion: String,
    val tipo: String
)

data class Resultado(
    val parejas: List<Pair<Int, Int>>,
    val eventos: List<Evento>
)

// --------------------------------------------------
// ALGORITMO GALE-SHAPLEY
// --------------------------------------------------

fun galeShapley(
    hombres: List<List<Int>>,
    mujeres: List<List<Int>>
): Resultado {

    val cantidadH = hombres.size
    val cantidadM = mujeres.size

    val parejaMujer = IntArray(cantidadM) { -1 }
    val siguientePropuesta = IntArray(cantidadH)

    val rankingMujeres = mujeres.map { preferencias ->
        preferencias.withIndex().associate {
            it.value to it.index
        }
    }

    val libres = ArrayDeque<Int>()

    for (h in 0 until cantidadH) {
        libres.addLast(h)
    }

    val eventos = mutableListOf<Evento>()

    while (libres.isNotEmpty()) {

        val h = libres.removeFirst()

        if (siguientePropuesta[h] >= hombres[h].size) {
            continue
        }

        val m = hombres[h][siguientePropuesta[h]]
        siguientePropuesta[h]++

        eventos.add(
            Evento(
                "H${h + 1} realiza una propuesta a M${m + 1}",
                "propuesta"
            )
        )

        val parejaActual = parejaMujer[m]

        if (parejaActual == -1) {

            parejaMujer[m] = h

            eventos.add(
                Evento(
                    "M${m + 1} acepta la propuesta de H${h + 1}",
                    "aceptacion"
                )
            )

        } else {

            val posicionNueva = rankingMujeres[m][h]!!
            val posicionActual = rankingMujeres[m][parejaActual]!!

            if (posicionNueva < posicionActual) {

                parejaMujer[m] = h
                libres.addLast(parejaActual)

                eventos.add(
                    Evento(
                        "M${m + 1} prefiere a H${h + 1}. H${parejaActual + 1} queda libre.",
                        "cambio"
                    )
                )

            } else {

                libres.addLast(h)

                eventos.add(
                    Evento(
                        "M${m + 1} rechaza la propuesta de H${h + 1}",
                        "rechazo"
                    )
                )
            }
        }
    }

    val parejas = mutableListOf<Pair<Int, Int>>()

    for (m in parejaMujer.indices) {
        val h = parejaMujer[m]

        if (h != -1) {
            parejas.add(h to m)
        }
    }

    return Resultado(parejas, eventos)
}

// --------------------------------------------------
// VALIDACIÓN DE PREFERENCIAS
// --------------------------------------------------

fun convertirPreferencias(
    texto: String,
    cantidad: Int
): List<Int>? {

    val valores = texto.split(",").map { it.trim() }

    if (valores.size != cantidad) return null

    val numeros = valores.map {
        it.toIntOrNull()?.minus(1) ?: return null
    }

    if (numeros.any { it !in 0 until cantidad }) return null

    if (numeros.toSet().size != cantidad) return null

    return numeros
}

// --------------------------------------------------
// ACTIVIDAD PRINCIPAL
// --------------------------------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PantallaPrincipal()
            }
        }
    }
}

// --------------------------------------------------
// PANTALLA PRINCIPAL
// --------------------------------------------------

@Composable
fun PantallaPrincipal() {

    var cantidadH by remember { mutableStateOf("3") }
    var cantidadM by remember { mutableStateOf("3") }

    var hombres by remember { mutableStateOf(listOf<String>()) }
    var mujeres by remember { mutableStateOf(listOf<String>()) }

    var nombresH by remember { mutableStateOf(listOf<String>()) }
    var nombresM by remember { mutableStateOf(listOf<String>()) }

    var resultado by remember { mutableStateOf<Resultado?>(null) }
    var mensaje by remember { mutableStateOf("") }

    var pasoActual by remember { mutableIntStateOf(0) }

    val nH = cantidadH.toIntOrNull() ?: 0
    val nM = cantidadM.toIntOrNull() ?: 0

    Scaffold(
        containerColor = Fondo
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Encabezado()

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // INTRODUCCIÓN

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {

                    Text(
                        "Panel de trabajo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarino
                    )

                    Text(
                        "Configura los participantes y descubre cómo funciona el emparejamiento estable.",
                        fontSize = 14.sp,
                        color = GrisTexto,
                        lineHeight = 21.sp
                    )
                }

                // PARTICIPANTES

                Tarjeta {

                    TituloSeccion(
                        numero = "01",
                        titulo = "Participantes",
                        subtitulo = "Define los conjuntos que participarán."
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        CampoNumero(
                            valor = cantidadH,
                            etiqueta = "Hombres (H)",
                            modifier = Modifier.weight(1f)
                        ) {
                            cantidadH = it
                            resultado = null
                        }

                        CampoNumero(
                            valor = cantidadM,
                            etiqueta = "Mujeres (M)",
                            modifier = Modifier.weight(1f)
                        ) {
                            cantidadM = it
                            resultado = null
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    BotonPrincipal(
                        texto = "Generar participantes"
                    ) {

                        if (nH !in 1..15 || nM !in 1..15) {

                            mensaje = "Ingresa cantidades entre 1 y 15."

                        } else {

                            hombres = List(nH) {
                                (1..nM).joinToString(", ")
                            }

                            mujeres = List(nM) {
                                (1..nH).joinToString(", ")
                            }

                            nombresH = List(nH) {
                                "H${it + 1}"
                            }

                            nombresM = List(nM) {
                                "M${it + 1}"
                            }

                            resultado = null
                            pasoActual = 0
                            mensaje = "Participantes generados correctamente."
                        }
                    }
                }

                // PREFERENCIAS

                if (hombres.isNotEmpty()) {


                    Tarjeta {

                        TituloSeccion(
                            numero = "02",
                            titulo = "Identificación",
                            subtitulo = "Personaliza los nombres de los participantes."
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "Conjunto H",
                            color = AzulPrincipal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        nombresH.forEachIndexed { index, nombre ->

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nuevo ->
                                    nombresH = nombresH.toMutableList().also {
                                        it[index] = nuevo
                                    }
                                    resultado = null
                                },
                                label = {
                                    Text("Nombre H${index + 1}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Conjunto M",
                            color = Morado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        nombresM.forEachIndexed { index, nombre ->

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nuevo ->
                                    nombresM = nombresM.toMutableList().also {
                                        it[index] = nuevo
                                    }
                                    resultado = null
                                },
                                label = {
                                    Text("Nombre M${index + 1}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))
                        }
                    }


                    Tarjeta {

                        TituloSeccion(
                            numero = "02",
                            titulo = "Preferencias de H",
                            subtitulo = "Orden de preferencia de cada hombre."
                        )

                        Spacer(Modifier.height(14.dp))

                        hombres.forEachIndexed { index, texto ->

                            CampoPreferencia(
                                etiqueta = "H${index + 1}",
                                valor = texto,
                                color = AzulPrincipal
                            ) { nuevo ->

                                hombres = hombres.toMutableList().also {
                                    it[index] = nuevo
                                }

                                resultado = null
                            }

                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    Tarjeta {

                        TituloSeccion(
                            numero = "03",
                            titulo = "Preferencias de M",
                            subtitulo = "Orden de preferencia de cada mujer."
                        )

                        Spacer(Modifier.height(14.dp))

                        mujeres.forEachIndexed { index, texto ->

                            CampoPreferencia(
                                etiqueta = "M${index + 1}",
                                valor = texto,
                                color = Morado
                            ) { nuevo ->

                                mujeres = mujeres.toMutableList().also {
                                    it[index] = nuevo
                                }

                                resultado = null
                            }

                            Spacer(Modifier.height(10.dp))
                        }

                        Text(
                            "Utiliza números separados por comas. " +
                                    "Ejemplo: 2, 1, 3 indica que el participante 2 " +
                                    "es la primera preferencia.",
                            fontSize = 12.sp,
                            color = GrisTexto,
                            lineHeight = 18.sp
                        )
                    }

                    // EJECUCIÓN

                    Tarjeta {

                        TituloSeccion(
                            numero = "04",
                            titulo = "Ejecutar algoritmo",
                            subtitulo = "Calcula el emparejamiento estable."
                        )

                        Spacer(Modifier.height(15.dp))

                        BotonPrincipal(
                            texto = "▶  Ejecutar Gale-Shapley",
                            color = Verde
                        ) {

                            val listaH = hombres.map {
                                convertirPreferencias(it, nM)
                            }

                            val listaM = mujeres.map {
                                convertirPreferencias(it, nH)
                            }

                            if (listaH.any { it == null } ||
                                listaM.any { it == null }) {

                                mensaje =
                                    "Error: revisa las preferencias. " +
                                            "Debes introducir todos los participantes, " +
                                            "sin repetir números y respetando los rangos."

                                resultado = null

                            } else {

                                resultado = galeShapley(
                                    listaH.filterNotNull(),
                                    listaM.filterNotNull()
                                )

                                pasoActual = 0
                                mensaje = "Algoritmo ejecutado correctamente."
                            }
                        }

                        if (mensaje.isNotEmpty()) {

                            Spacer(Modifier.height(12.dp))

                            Text(
                                mensaje,
                                fontSize = 13.sp,
                                color = if (mensaje.startsWith("Error"))
                                    Rojo else Verde,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // RESULTADOS

                resultado?.let { res ->

                    Tarjeta {

                        TituloSeccion(
                            numero = "05",
                            titulo = "Árbol de ejecución",
                            subtitulo = "Seguimiento de las operaciones del algoritmo."
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "Paso ${pasoActual + 1} de ${res.eventos.size}",
                                fontWeight = FontWeight.Bold,
                                color = AzulPrincipal
                            )

                            EtiquetaEstado(
                                res.eventos.getOrNull(pasoActual)?.tipo ?: ""
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        if (res.eventos.isNotEmpty()) {

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEAF2FF)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    res.eventos[pasoActual].descripcion,
                                    modifier = Modifier.padding(18.dp),
                                    color = AzulMarino,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 23.sp
                                )

                            }

                            Spacer(Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                                OutlinedButton(
                                    onClick = {
                                        if (pasoActual > 0) pasoActual--
                                    },
                                    enabled = pasoActual > 0,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Anterior")
                                }

                                Button(
                                    onClick = {
                                        if (pasoActual < res.eventos.lastIndex)
                                            pasoActual++
                                    },
                                    enabled = pasoActual < res.eventos.lastIndex,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AzulPrincipal
                                    )
                                ) {
                                    Text("Siguiente")
                                }
                                OutlinedButton(
                                    onClick = {
                                        pasoActual = 0
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reiniciar")
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            "Historial completo",
                            fontWeight = FontWeight.Bold,
                            color = AzulMarino,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        res.eventos.forEachIndexed { index, evento ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pasoActual)
                                                AzulClaro else Fondo
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Text(
                                        "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarino
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colorEvento(evento.tipo))
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    evento.descripcion,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    color = GrisTexto,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // EMPAREJAMIENTO FINAL

                    Tarjeta {

                        TituloSeccion(
                            numero = "06",
                            titulo = "Resultado final",
                            subtitulo = "Parejas obtenidas mediante Gale-Shapley."
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "${res.parejas.size} parejas estables obtenidas",
                            color = Verde,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        res.parejas.forEach { (h, m) ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEAF6F0))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    nombresH.getOrElse(h) { "H${h + 1}" },
                                    color = AzulPrincipal,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "↔",
                                    color = Verde,
                                    fontSize = 20.sp
                                )

                                Text(
                                    nombresM.getOrElse(m) { "M${m + 1}" },
                                    color = Morado,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (res.parejas.size < nH || res.parejas.size < nM) {

                            Text(
                                "Algunos participantes quedaron sin pareja.",
                                fontSize = 12.sp,
                                color = GrisTexto
                            )
                        }
                    }

                    // GRAFO

                    Tarjeta {

                        TituloSeccion(
                            numero = "07",
                            titulo = "Grafo bipartito",
                            subtitulo = "Representación de los emparejamientos finales."
                        )

                        Spacer(Modifier.height(15.dp))

                        GrafoEmparejamiento(
                            cantidadH = nH,
                            cantidadM = nM,
                            parejas = res.parejas,
                            nombresH = nombresH,
                            nombresM = nombresM
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {

                            Leyenda(AzulPrincipal, "Conjunto H")
                            Leyenda(Morado, "Conjunto M")
                        }
                    }
                }

                // PIE DE PÁGINA

                Spacer(Modifier.height(8.dp))

                Text(
                    "TEORÍA DE GRÁFICAS  •  ALGORITMO GALE-SHAPLEY",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = GrisTexto,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// --------------------------------------------------
// ENCABEZADO
// --------------------------------------------------

@Composable
fun Encabezado() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AzulMarino,
                        Color(0xFF174B8E),
                        AzulPrincipal
                    )
                )
            )
            .padding(horizontal = 22.dp, vertical = 30.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Text(
                "TEORÍA DE GRÁFICAS",
                color = Color(0xFFB8D5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                "GALE–SHAPLEY",
                color = Blanco,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                "Sistema de emparejamiento estable",
                color = Color(0xFFE1EDFF),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            ) {

                Text(
                    "VISUALIZACIÓN DEL ALGORITMO",
                    color = Blanco,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// --------------------------------------------------
// COMPONENTES DE INTERFAZ
// --------------------------------------------------

@Composable
fun Tarjeta(
    contenido: @Composable ColumnScope.() -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blanco
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            content = contenido
        )
    }
}

@Composable
fun TituloSeccion(
    numero: String,
    titulo: String,
    subtitulo: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(39.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AzulClaro),
            contentAlignment = Alignment.Center
        ) {

            Text(
                numero,
                color = AzulPrincipal,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {

            Text(
                titulo,
                color = AzulMarino,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                subtitulo,
                color = GrisTexto,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CampoNumero(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    alCambiar: (String) -> Unit
) {

    OutlinedTextField(
        value = valor,
        onValueChange = {
            alCambiar(it.filter { c -> c.isDigit() })
        },
        label = { Text(etiqueta, fontSize = 12.sp) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CampoPreferencia(
    etiqueta: String,
    valor: String,
    color: Color,
    alCambiar: (String) -> Unit
) {

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = {
            Text(
                "Preferencias de $etiqueta",
                fontSize = 12.sp
            )
        },
        leadingIcon = {

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    etiqueta,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

@Composable
fun BotonPrincipal(
    texto: String,
    color: Color = AzulPrincipal,
    accion: () -> Unit
) {

    Button(
        onClick = accion,
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {

        Text(
            texto,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EtiquetaEstado(tipo: String) {

    val color = colorEvento(tipo)

    val texto = when (tipo) {
        "propuesta" -> "PROPUESTA"
        "aceptacion" -> "ACEPTACIÓN"
        "rechazo" -> "RECHAZO"
        "cambio" -> "CAMBIO"
        else -> "FINALIZADO"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {

        Text(
            texto,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun colorEvento(tipo: String): Color {

    return when (tipo) {
        "propuesta" -> AzulPrincipal
        "aceptacion" -> Verde
        "rechazo" -> Rojo
        "cambio" -> Morado
        else -> GrisTexto
    }
}

@Composable
fun Leyenda(
    color: Color,
    texto: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            texto,
            fontSize = 11.sp,
            color = GrisTexto
        )
    }
}

// --------------------------------------------------
// GRAFO BIPARTITO
// --------------------------------------------------

@Composable
fun GrafoEmparejamiento(
    cantidadH: Int,
    cantidadM: Int,
    parejas: List<Pair<Int, Int>>,
    nombresH: List<String>,
    nombresM: List<String>
) {

    val alto = (maxOf(cantidadH, cantidadM) * 75 + 65)
        .coerceAtLeast(160)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(alto.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F9FD))
            .border(
                1.dp,
                Color(0xFFE1EAF5),
                RoundedCornerShape(14.dp)
            )
    ) {

        val xH = size.width * 0.23f
        val xM = size.width * 0.77f

        val separacionH = size.height / (cantidadH + 1)
        val separacionM = size.height / (cantidadM + 1)

        fun posicionH(h: Int) =
            separacionH * (h + 1)

        fun posicionM(m: Int) =
            separacionM * (m + 1)

        // ARISTAS


        parejas.forEach { (h, m) ->

            drawLine(
                color = Color(0xFF8B79C6),
                start = androidx.compose.ui.geometry.Offset(
                    xH, posicionH(h)
                ),
                end = androidx.compose.ui.geometry.Offset(
                    xM, posicionM(m)
                ),
                strokeWidth = 3f
            )
        }


        // NODOS H

        for (h in 0 until cantidadH) {

            val y = posicionH(h)


            drawCircle(
                color = Color(0xFFB8D5FF),
                radius = 23f,
                center = androidx.compose.ui.geometry.Offset(xH, y)
            )

            drawCircle(
                color = AzulPrincipal,
                radius = 19f,
                center = androidx.compose.ui.geometry.Offset(xH, y)
            )



            drawContext.canvas.nativeCanvas.drawText(
                "H${h + 1}",
                xH,
                y + 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            drawContext.canvas.nativeCanvas.drawText(
                nombresH.getOrElse(h) { "H${h + 1}" }.take(10),
                xH,
                y + 36f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(16, 42, 86)
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

        }

        // NODOS M

        for (m in 0 until cantidadM) {

            val y = posicionM(m)


            drawCircle(
                color = Color(0xFFD9C8F5),
                radius = 23f,
                center = androidx.compose.ui.geometry.Offset(xM, y)
            )

            drawCircle(
                color = Morado,
                radius = 19f,
                center = androidx.compose.ui.geometry.Offset(xM, y)
            )


            drawContext.canvas.nativeCanvas.drawText(
                "M${m + 1}",
                xM,
                y + 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            drawContext.canvas.nativeCanvas.drawText(
                nombresM.getOrElse(m) { "M${m + 1}" }.take(10),
                xM,
                y + 36f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(75, 45, 125)
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

        }

        // TÍTULOS DE LOS CONJUNTOS

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(16, 42, 86)
            textSize = 14.sp.toPx()
            isAntiAlias = true
            isFakeBoldText = true
        }

        drawContext.canvas.nativeCanvas.drawText(
            "Hombres",
            xH - 27f,
            23f,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Mujeres",
            xM - 27f,
            23f,
            paint
        )
    }
}
