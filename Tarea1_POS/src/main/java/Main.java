/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.tec.minipc;

import com.tec.minipc.core.Assembler;
import com.tec.minipc.core.AssemblyException;
import com.tec.minipc.core.Cpu;
import com.tec.minipc.core.PCB;
import com.tec.minipc.model.Instruction;
import com.tec.minipc.model.Memory;
import com.tec.minipc.model.RegisterName;
import com.tec.minipc.model.Registers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Versión de consola del Mini PC. Sirve para probar todo el motor
 * (Assembler + Memory + PCB + Cpu) antes de construir la interfaz gráfica.
 */
public class Main {

    private static final int TAMANO_MEMORIA = 256;
    private static final int TAMANO_SO = 64; // 0-63 S.O., 64-255 Usuario

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String ruta;
        if (args.length > 0) {
            ruta = args[0];
        } else {
            System.out.print("Ruta del archivo .asm: ");
            ruta = scanner.nextLine().trim();
        }

        List<Instruction> instrucciones;
        try {
            instrucciones = Assembler.loadFromFile(new File(ruta));
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
            return;
        } catch (AssemblyException e) {
            System.out.println("El archivo tiene errores de formato:");
            for (String error : e.getErrores()) {
                System.out.println(" - " + error);
            }
            return;
        }

        Memory memoria = new Memory(TAMANO_MEMORIA, TAMANO_SO);
        int direccionBase = memoria.loadProgram(instrucciones);

        Registers registros = new Registers();
        registros.setPc(direccionBase);

        PCB pcb = new PCB(1, new File(ruta).getName(), direccionBase, memoria.getUserEnd(), instrucciones.size());
        Cpu cpu = new Cpu(memoria, registros, pcb);

        System.out.println("\nPrograma cargado: " + instrucciones.size() + " instrucciones, desde la dirección "
                + direccionBase + " hasta " + (direccionBase + instrucciones.size() * 2 - 1));

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    ejecutarUnPaso(cpu, registros);
                    break;
                case "2":
                    ejecutarTodo(cpu, registros);
                    break;
                case "3":
                    mostrarRegistros(registros);
                    break;
                case "4":
                    System.out.println(pcb);
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }

        scanner.close();
        System.out.println("Fin del programa.");
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Mini PC ---");
        System.out.println("1) Ejecutar siguiente instrucción (paso a paso)");
        System.out.println("2) Ejecutar todo (automático)");
        System.out.println("3) Ver registros");
        System.out.println("4) Ver PCB");
        System.out.println("0) Salir");
        System.out.print("Opción: ");
    }

    private static void ejecutarUnPaso(Cpu cpu, Registers registros) {
        if (cpu.isTerminado()) {
            System.out.println("El programa ya terminó.");
            return;
        }
        Instruction ejecutada = cpu.step();
        System.out.println("Se ejecutó: " + ejecutada.getSourceLine());
        mostrarRegistros(registros);
        if (cpu.isTerminado()) {
            System.out.println("Programa terminado.");
        }
    }

    private static void ejecutarTodo(Cpu cpu, Registers registros) {
        if (cpu.isTerminado()) {
            System.out.println("El programa ya terminó.");
            return;
        }
        int cantidad = cpu.runAll();
        System.out.println("Se ejecutaron " + cantidad + " instrucciones.");
        mostrarRegistros(registros);
    }

    private static void mostrarRegistros(Registers registros) {
        System.out.printf("AC=%d  AX=%d  BX=%d  CX=%d  DX=%d  PC=%d%n",
                registros.getAc(),
                registros.get(RegisterName.AX),
                registros.get(RegisterName.BX),
                registros.get(RegisterName.CX),
                registros.get(RegisterName.DX),
                registros.getPc());
    }
}
