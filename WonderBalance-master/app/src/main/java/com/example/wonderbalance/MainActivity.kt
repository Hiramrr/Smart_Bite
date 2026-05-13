package com.example.wonderbalance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.wonderbalance.databinding.ActivityMainBinding
import com.example.wonderbalance.util.GestorSesion

class MainActivity : AppCompatActivity() {

    private lateinit var enlace: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enlace = ActivityMainBinding.inflate(layoutInflater)
        setContentView(enlace.root)

        val gestorSesion = GestorSesion(this)
        val fragmentoContenedor = supportFragmentManager
            .findFragmentById(R.id.contenedor_nav) as NavHostFragment
        val controladorNav = fragmentoContenedor.navController

        if (gestorSesion.haySesionActiva()) {
            controladorNav.navigate(R.id.fragmentoDashboard)
        }

        enlace.navInferior.setupWithNavController(controladorNav)

        // Pantallas que NO muestran barra inferior
        val pantallasOcultas = setOf(
            R.id.fragmentoAcceso,
            R.id.fragmentoRegistro,
            R.id.fragmentoTransaccion,
            R.id.fragmentoHistorial,
            R.id.fragmentoDetalleTransaccion,
            R.id.fragmentoNuevoPresupuesto,
            R.id.fragmentoNuevaMeta
        )

        controladorNav.addOnDestinationChangedListener { _, destino, _ ->
            if (destino.id in pantallasOcultas) {
                enlace.navInferior.visibility = android.view.View.GONE
            } else {
                enlace.navInferior.visibility = android.view.View.VISIBLE
            }
        }
    }

    // Botón físico atrás navega correctamente
    override fun onSupportNavigateUp(): Boolean {
        val fragmentoContenedor = supportFragmentManager
            .findFragmentById(R.id.contenedor_nav) as NavHostFragment
        return fragmentoContenedor.navController.navigateUp() || super.onSupportNavigateUp()
    }
}