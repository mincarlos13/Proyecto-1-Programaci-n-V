import java.io.FileWriter;
import java.io.IOException;

public class Simulador {
    public static void main(String[] args) {
        int cambioContexto = 2;
        int quantum = 4;
        int procesosPorCola = 2;
        int numeroColas = 2;

        AdministradorMLQ admin = new AdministradorMLQ(cambioContexto, quantum, procesosPorCola);
        admin.crearColas(numeroColas);
        admin.crearProcesosEnColas();

        int tiempoTotal = 0;
        int totalCambiosContexto = 0;
        int totalBloqueo = 0;
        int iteracion = 1;

        try (FileWriter writer = new FileWriter("resultado.txt")) {
            writer.write("===== SIMULACIÓN MLQ =====\n\n");

            boolean hayProcesos;
            do {
                hayProcesos = false;
                imprimirEncabezado(writer, iteracion);

                for (Cola cola : admin.getColas()) {
                    for (Proceso p : cola.getProcesos()) {
                        if (p.getEstado() != EstadoProceso.Terminado) {
                            hayProcesos = true;
                            p.ejecutar(admin.getQuantum());
                            p.simularBloqueado();
                            tiempoTotal += admin.getQuantum();
                            totalCambiosContexto += p.getCambiosDeContexto();
                            totalBloqueo += p.getTiempoBloqueo();

                            imprimirProceso(writer, p);
                        } else {
                            p.aumentarEspera();
                            imprimirProceso(writer, p);
                        }
                    }
                }

                System.out.println("--------------------------------------------------------------------------");
                writer.write("--------------------------------------------------------------------------\n");
                iteracion++;
            } while (hayProcesos);

            // ===== Resumen final =====
            String resumen = "\n===== RESUMEN FINAL =====\n" +
                    "Total de iteraciones: " + (iteracion - 1) + "\n" +
                    "Tiempo total de ejecución: " + tiempoTotal + "\n" +
                    "Tiempo total de cambios de contexto: " + totalCambiosContexto + "\n" +
                    "Tiempo total de bloqueo: " + totalBloqueo + "\n";

            System.out.println(resumen);
            writer.write(resumen);

        } catch (IOException e) {
            System.out.println("Error al escribir el archivo: " + e.getMessage());
        }
    }

    private static void imprimirEncabezado(FileWriter writer, int iteracion) throws IOException {
        String header = String.format(
            "| %-5s | %-4s | %-6s | %-10s | %-8s | %-5s | %-7s | %-7s | %-10s |",
            "ID", "Cola", "Inst.", "Estado", "TimeCola", "CDC", "Bloqueo", "CPU", "Total"
        );
        System.out.println("Iteración " + iteracion);
        System.out.println(header);
        writer.write("Iteración " + iteracion + "\n");
        writer.write(header + "\n");

        String separador = new String(new char[header.length()]).replace('\0', '-');
        System.out.println(separador);
        writer.write(separador + "\n");
    }

    private static void imprimirProceso(FileWriter writer, Proceso p) throws IOException {
        String linea = String.format(
            "| P%-4d | %-4d | %-6d | %-10s | %-8d | %-5d | %-7d | %-7d | %-10d |",
            p.getId(), p.getIdCola(), p.getNumeroInstrucciones(),
            p.getEstado(), p.getTiempoEspera(), p.getCambiosDeContexto(),
            p.getTiempoBloqueo(), p.getTiempoCPU(), p.getTiempoTotal()
        );
        System.out.println(linea);
        writer.write(linea + "\n");
    }
}
