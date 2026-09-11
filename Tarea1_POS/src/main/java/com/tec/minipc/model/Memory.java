/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tec.minipc.model;

import java.util.List;

/**
 * Memoria principal del Mini PC.
 *
 * Se divide en dos zonas contiguas:
 *   [0 .. osSize-1]        -> espacio del Sistema Operativo (reservado, ej. para el BCP)
 *   [osSize .. totalSize-1] -> espacio de Usuario (donde se carga el programa .asm)
 *
 * Cada celda guarda un byte (0-255). Una instrucción ocupa 2 celdas consecutivas.
 * Una celda con valor -1 se considera "vacía" (nunca escrita).
 */
public class Memory {

    public static final int TAMANO_MINIMO = 128;

    private final int totalSize;
    private final int osSize;      // cantidad de celdas reservadas para el S.O. (desde 0)
    private final int[] cells;

    public Memory(int totalSize, int osSize) {
        if (totalSize < TAMANO_MINIMO) {
            throw new IllegalArgumentException(
                    "El tamaño de memoria debe ser al menos " + TAMANO_MINIMO + " (recibido: " + totalSize + ")");
        }
        if (osSize < 0 || osSize >= totalSize) {
            throw new IllegalArgumentException(
                    "El espacio de S.O. (" + osSize + ") debe ser mayor a 0 y menor que el total (" + totalSize + ")");
        }
        this.totalSize = totalSize;
        this.osSize = osSize;
        this.cells = new int[totalSize];
        clear();
    }

    public void clear() {
        for (int i = 0; i < cells.length; i++) {
            cells[i] = -1;
        }
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getOsStart() {
        return 0;
    }

    public int getOsEnd() {
        return osSize - 1;
    }

    public int getUserStart() {
        return osSize;
    }

    public int getUserEnd() {
        return totalSize - 1;
    }

    public boolean isValidAddress(int address) {
        return address >= 0 && address < totalSize;
    }

    public boolean isOsAddress(int address) {
        return address >= getOsStart() && address <= getOsEnd();
    }

    public boolean isUserAddress(int address) {
        return address >= getUserStart() && address <= getUserEnd();
    }

    public void write(int address, int value) {
        requireValid(address);
        cells[address] = value & 0xFF;
    }

    public int read(int address) {
        requireValid(address);
        int v = cells[address];
        return (v == -1) ? 0 : v;
    }

    public boolean isEmpty(int address) {
        requireValid(address);
        return cells[address] == -1;
    }

    private void requireValid(int address) {
        if (!isValidAddress(address)) {
            throw new IndexOutOfBoundsException(
                    "Dirección de memoria fuera de rango: " + address + " (0.." + (totalSize - 1) + ")");
        }
    }

    /**
     * Carga una lista de instrucciones ya parseadas a partir de userStart,
     * usando 2 celdas por instrucción. Valida que el programa quepa en el
     * espacio de Usuario disponible.
     *
     * @param instrucciones lista de instrucciones ya parseadas del .asm
     * @return la dirección (PC) donde debe iniciar la ejecución.
     */
    public int loadProgram(List<Instruction> instrucciones) {
        int startAddress = getUserStart();
        int requiredCells = instrucciones.size() * 2;
        int availableCells = getUserEnd() - getUserStart() + 1;

        if (requiredCells > availableCells) {
            throw new IllegalStateException(
                    "El programa (" + instrucciones.size() + " instrucciones, " + requiredCells
                            + " celdas) no cabe en el espacio de Usuario disponible (" + availableCells + " celdas)");
        }

        int address = startAddress;
        for (Instruction ins : instrucciones) {
            int[] bytes = ins.encode();
            write(address, bytes[0]);
            write(address + 1, bytes[1]);
            address += 2;
        }
        return startAddress;
    }
}