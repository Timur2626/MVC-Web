public class DJWebView {

    public String render(int bpm, String message) {
        boolean running = bpm > 0;

        String status = running
                ? "Текущий темп: " + bpm + " BPM"
                : "Ритм выключен";

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>DJ — веб MVC</title>

                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 60px auto;
                            padding: 20px;
                        }

                        button, input {
                            padding: 10px;
                            margin: 5px;
                            font-size: 16px;
                        }

                        .message {
                            color: darkred;
                        }
                    </style>
                </head>

                <body>
                    <h1>DJ — управление ритмом</h1>

                    <h2>{{status}}</h2>

                    <p class="message">{{message}}</p>

                    <form method="post" action="/">
                        <button name="action" value="start"
                                {{startDisabled}}>
                            Старт
                        </button>

                        <button name="action" value="stop"
                                {{controlsDisabled}}>
                            Стоп
                        </button>
                    </form>

                    <form method="post" action="/">
                        <button name="action" value="decrease"
                                {{controlsDisabled}}>
                            − Уменьшить темп
                        </button>

                        <button name="action" value="increase"
                                {{controlsDisabled}}>
                            + Увеличить темп
                        </button>
                    </form>

                    <form method="post" action="/">
                        <label>
                            Темп:
                            <input type="number"
                                   name="bpm"
                                   min="30"
                                   max="300"
                                   value="{{bpm}}"
                                   required
                                   {{controlsDisabled}}>
                        </label>

                        <button name="action" value="set"
                                {{controlsDisabled}}>
                            Установить
                        </button>
                    </form>
                </body>
                </html>
                """
                .replace("{{status}}", status)
                .replace("{{message}}", message)
                .replace("{{bpm}}", String.valueOf(running ? bpm : 90))
                .replace("{{startDisabled}}", running ? "disabled" : "")
                .replace("{{controlsDisabled}}", running ? "" : "disabled");
    }
}