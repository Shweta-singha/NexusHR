import { useState, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { askHrChat } from './api'

interface Message {
  role: 'user' | 'assistant'
  text: string
}

export default function ChatWidget() {
  const [question, setQuestion] = useState('')
  const [messages, setMessages] = useState<Message[]>([])

  const askMutation = useMutation({
    mutationFn: askHrChat,
    onSuccess: (res) => {
      setMessages((prev) => [...prev, { role: 'assistant', text: res.answer }])
    },
    onError: () => {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', text: 'Something went wrong answering that — please try again.' },
      ])
    },
  })

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!question.trim()) return
    setMessages((prev) => [...prev, { role: 'user', text: question }])
    askMutation.mutate(question)
    setQuestion('')
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
      <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Ask HR</h2>

      <div
        role="log"
        aria-live="polite"
        aria-label="Chat conversation"
        className="mb-3 flex max-h-96 flex-col gap-2 overflow-y-auto"
      >
        {messages.length === 0 && (
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Ask a question about leave, remote work, expenses, conduct, or performance policy.
          </p>
        )}
        {messages.map((m, i) => (
          <div
            key={i}
            className={
              m.role === 'user'
                ? 'ml-auto max-w-[80%] rounded-lg bg-slate-900 px-3 py-2 text-sm text-white dark:bg-slate-100 dark:text-slate-900'
                : 'mr-auto max-w-[80%] whitespace-pre-wrap rounded-lg bg-slate-100 px-3 py-2 text-sm text-slate-900 dark:bg-slate-700 dark:text-slate-100'
            }
          >
            <span className="sr-only">{m.role === 'user' ? 'You said: ' : 'Assistant said: '}</span>
            {m.text}
          </div>
        ))}
        {askMutation.isPending && (
          <div className="mr-auto max-w-[80%] rounded-lg bg-slate-100 px-3 py-2 text-sm text-slate-500 dark:bg-slate-700 dark:text-slate-400">
            Thinking…
          </div>
        )}
      </div>

      <form onSubmit={handleSubmit} className="flex gap-2">
        <label htmlFor="hr-chat-input" className="sr-only">Ask a question</label>
        <input
          id="hr-chat-input"
          type="text"
          placeholder="Type a question…"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
        />
        <button
          type="submit"
          disabled={askMutation.isPending || !question.trim()}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
        >
          Send
        </button>
      </form>
    </div>
  )
}
