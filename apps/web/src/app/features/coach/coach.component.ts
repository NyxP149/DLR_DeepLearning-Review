import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import {
  AdaptationApiService,
  AdaptationDecision,
  AdaptationInsights,
  AdaptationRecommendation,
  Calibration,
  PretestGainSummary,
  Retention
} from '../../core/api/adaptation-api.service';
import { TutorApiService, TutorRole } from '../../core/api/tutor-api.service';

@Component({
  selector: 'dlr-coach',
  imports: [DatePipe, RouterLink],
  template: `
    <header class="page-header">
      <p>Coach adaptatif V2</p>
      <h1>Ta prochaine action, expliquée</h1>
      <span>DLR propose. Tu gardes toujours le contrôle du parcours.</span>
    </header>

    @if (loading()) {
      <p class="state">Analyse locale de ta progression…</p>
    } @else if (error()) {
      <p class="state error" role="alert">{{ error() }}</p>
    } @else {
      @if (recommendation(); as item) {
        <section class="recommendation">
          <div class="eyebrow"><span>{{ difficultyLabel(item.difficulty) }}</span><small>Expire le {{ item.expiresAt | date:'dd/MM à HH:mm' }}</small></div>
          <h2>{{ item.targetedConcepts.join(' · ') }}</h2>
          <p class="reason">{{ item.reason }}</p>
          <article><small>Activité proposée</small><strong>{{ item.proposedActivity }}</strong></article>
          <article><small>Bénéfice attendu</small><strong>{{ item.expectedBenefit }}</strong></article>
          <details open><summary>Pourquoi cette proposition ?</summary><ul>@for (factor of item.factors; track factor) { <li>{{ factor }}</li> }</ul></details>
          <div class="actions">
            @if (item.status === 'ACCEPTED') {
              <span class="accepted" role="status">✓ Proposition acceptée</span>
            } @else {
              <button class="primary" type="button" (click)="decide('ACCEPT')" [disabled]="deciding()">Accepter</button>
              <button type="button" (click)="decide('POSTPONE')" [disabled]="deciding()">Reporter 3 jours</button>
            }
            <button type="button" (click)="decide('REPLACE')" [disabled]="deciding()">Autre proposition</button>
            <button class="quiet" type="button" (click)="decide('IGNORE')" [disabled]="deciding()">Ignorer</button>
            <a [routerLink]="['/labs', item.labCode]">Ouvrir {{ item.labCode }} →</a>
          </div>
          @if (feedback()) { <p class="feedback" role="status">{{ feedback() }}</p> }
        </section>
      }

      @if (insights(); as data) {
        <section class="analytics">
          <div class="section-title"><div><p>Signaux locaux</p><h2>Autonomie et transfert</h2></div><span>Estimations, jamais des verdicts</span></div>
          <div class="metrics">
            <article><span>Autonomie</span><strong>{{ data.autonomyScore }} %</strong><i><b [style.width.%]="data.autonomyScore"></b></i></article>
            <article><span>Dépendance aux indices</span><strong>{{ data.hintDependencyPercent }} %</strong><i><b [style.width.%]="data.hintDependencyPercent"></b></i></article>
            <article><span>Transfert confirmé</span><strong>{{ data.transferScore }} %</strong><i><b [style.width.%]="data.transferScore"></b></i></article>
            <article><span>Reste estimé</span><strong>{{ data.estimatedWeeksRemaining }} sem.</strong><small>au rythme récent</small></article>
          </div>
          <ul>@for (factor of data.factors; track factor) { <li>{{ factor }}</li> }</ul>
          <p class="disclaimer">{{ data.disclaimer }}</p>
        </section>
      }
      @if (calibration(); as cal) {
        <section class="calibration" aria-labelledby="calibration-title">
          <div class="section-title"><div><p>Métacognition</p><h2 id="calibration-title">Calibration : ce que tu crois savoir</h2></div><span>{{ cal.totalAnswers }} réponse{{ cal.totalAnswers > 1 ? 's' : '' }} avec confiance</span></div>
          <p class="verdict" [class.alert]="cal.verdict === 'OVERCONFIDENT'" [class.good]="cal.verdict === 'CALIBRATED' || cal.verdict === 'UNDERCONFIDENT'" role="status">{{ cal.message }}</p>
          @if (cal.totalAnswers > 0) {
            <div class="levels">
              @for (level of cal.levels; track level.confidence) {
                <article>
                  <header><strong>{{ confidenceLabel(level.confidence) }}</strong><span>{{ level.total }} réponse{{ level.total > 1 ? 's' : '' }}</span></header>
                  <div class="stack" role="img" [attr.aria-label]="level.correct + ' justes, ' + level.partial + ' partielles, ' + level.wrong + ' fausses'">
                    <i class="ok" [style.flex-grow]="level.correct"></i><i class="part" [style.flex-grow]="level.partial"></i><i class="ko" [style.flex-grow]="level.wrong"></i>
                  </div>
                  <small>{{ level.total === 0 ? 'Aucune réponse' : level.correctPercent + ' % justes · ' + level.partial + ' partielles · ' + level.wrong + ' fausses' }}</small>
                </article>
              }
            </div>
            <p class="reading">Une bonne calibration se voit à des barres vertes de plus en plus longues quand la confiance monte.
              @if (cal.levels[2].total > 0) { Quand tu es sûr·e, tu te trompes ou n'as qu'à moitié raison dans {{ cal.overconfidencePercent }} % des cas. }
              @if (cal.levels[0].total > 0) { Quand tu n'es pas sûr·e, tu as pourtant raison dans {{ cal.underconfidencePercent }} % des cas. }</p>
            <ul class="sources">@for (source of cal.sources; track source.code) { <li><strong>{{ source.code === 'QUIZ' ? 'Quiz des labos' : 'Révisions' }}</strong> : {{ source.total }} réponse{{ source.total > 1 ? 's' : '' }}{{ source.total > 0 ? ', ' + source.correctPercent + ' % justes' : '' }}</li> }</ul>
          }
          @if (cal.trend.periods.length > 0) {
            <div class="trend">
              <h3>Évolution semaine après semaine</h3>
              <p class="verdict" [class.alert]="cal.trend.verdict === 'WORSENING'" [class.good]="cal.trend.verdict === 'IMPROVING'" role="status">{{ cal.trend.message }}</p>
              <div class="weeks">
                @for (week of cal.trend.periods; track week.weekStart) {
                  <article [class.faint]="!week.reliable">
                    <header><strong>Semaine du {{ week.weekStart | date:'dd/MM' }}</strong><span>{{ week.total }} réponse{{ week.total > 1 ? 's' : '' }}</span></header>
                    <div class="duo">
                      <div><span>Confiance</span><em>{{ week.confidencePercent }} %</em><i [attr.aria-label]="week.confidencePercent + ' % de confiance'"><b class="before" [style.width.%]="week.confidencePercent"></b></i></div>
                      <div><span>Réussite</span><em>{{ week.accuracyPercent }} %</em><i [attr.aria-label]="week.accuracyPercent + ' % de réussite'"><b class="after" [style.width.%]="week.accuracyPercent"></b></i></div>
                    </div>
                    <small>{{ gapLabel(week.gapPoints) }}{{ week.reliable ? '' : ' · trop peu de réponses pour compter' }}</small>
                  </article>
                }
              </div>
              <p class="reading">La confiance est la moyenne de tes réponses (pas sûr·e 33 %, plutôt sûr·e 67 %, sûr·e 100 %). Plus les deux barres se rejoignent, mieux tu te connais. Les semaines avec moins de 5 réponses sont estompées et ne comptent pas dans la tendance.</p>
            </div>
          }
          @if (cal.blindSpots.length > 0) {
            <div class="blind-spots">
              <h3>Angles morts à retravailler</h3>
              <p>Des questions où tu étais sûr·e de toi sans avoir raison, les plus récentes d'abord.</p>
              <ul>
                @for (spot of cal.blindSpots; track spot.labCode + spot.questionId) {
                  <li><span class="tag">{{ spot.outcome === 'WRONG' ? 'Fausse' : 'Partielle' }}</span><div><strong>{{ spot.prompt }}</strong><small>{{ spot.source === 'QUIZ' ? 'Quiz' : 'Révision' }} · {{ spot.answeredAt | date:'dd/MM/yyyy' }}</small></div><a [routerLink]="['/labs', spot.labCode]">{{ spot.labCode }} →</a></li>
                }
              </ul>
            </div>
          }
        </section>
      }
      @if (retention(); as ret) {
        <section class="calibration retention" aria-labelledby="retention-title">
          <div class="section-title"><div><p>Mémoire</p><h2 id="retention-title">Rétention : ce qu'il en reste après quelques jours</h2></div><span>{{ ret.totalRecalls }} rappel{{ ret.totalRecalls > 1 ? 's' : '' }} différé{{ ret.totalRecalls > 1 ? 's' : '' }}</span></div>
          <p class="verdict" [class.alert]="ret.verdict === 'FRAGILE'" [class.good]="ret.verdict === 'DURABLE'" role="status">{{ ret.message }}</p>
          @if (ret.totalRecalls > 0) {
            <div class="delays">
              @for (bucket of ret.buckets; track bucket.label) {
                <article>
                  <header><strong>{{ bucket.label }}</strong><span>{{ bucket.total }} rappel{{ bucket.total > 1 ? 's' : '' }}</span></header>
                  @if (bucket.total > 0) {
                    <div class="duo">
                      <div><span>Au quiz initial</span><em>{{ bucket.initialSamples > 0 ? bucket.initialPercent + ' %' : '—' }}</em><i [attr.aria-label]="bucket.initialPercent + ' % au quiz initial'"><b class="before" [style.width.%]="bucket.initialPercent"></b></i></div>
                      <div><span>Au rappel</span><em>{{ bucket.correctPercent }} %</em><i [attr.aria-label]="bucket.correctPercent + ' % au rappel'"><b class="after" [style.width.%]="bucket.correctPercent"></b></i></div>
                    </div>
                  } @else {
                    <small>Aucun rappel</small>
                  }
                </article>
              }
            </div>
            <p class="reading">Les barres comparent, sur les mêmes questions, la réussite à la première réponse du quiz et la réussite au rappel sans le cours. Un écart qui se creuse avec le délai signale un oubli.</p>
          }
          @if (ret.retainedLabs.length > 0) {
            <p class="retained"><strong>Retenus durablement</strong> (rappel juste 7 jours ou plus après la fin) :
              @for (lab of ret.retainedLabs; track lab.labCode) { <a [routerLink]="['/labs', lab.labCode]">{{ lab.labCode }}</a> }</p>
          }
          @if (ret.forgotten.length > 0) {
            <div class="blind-spots">
              <h3>Oublis à revoir</h3>
              <p>Des questions réussies au quiz initial mais plus acquises au dernier rappel, les plus récentes d'abord.</p>
              <ul>
                @for (item of ret.forgotten; track item.labCode + item.questionId) {
                  <li><span class="tag">{{ item.outcome === 'WRONG' ? 'Oubliée' : 'Partielle' }}</span><div><strong>{{ item.prompt }}</strong><small>Rappel à {{ item.delayDays }} jour{{ item.delayDays > 1 ? 's' : '' }} · {{ item.answeredAt | date:'dd/MM/yyyy' }}</small></div><a [routerLink]="['/labs', item.labCode]">{{ item.labCode }} →</a></li>
                }
              </ul>
            </div>
          }
        </section>
      }
      @if (pretestGain(); as pre) {
        <section class="calibration pretest-gain" aria-labelledby="pretest-gain-title">
          <div class="section-title"><div><p>Apprentissage</p><h2 id="pretest-gain-title">Quiz de départ : ce que le cours t'apporte</h2></div><span>{{ pre.labsCompared }} labo{{ pre.labsCompared > 1 ? 's' : '' }} comparé{{ pre.labsCompared > 1 ? 's' : '' }}</span></div>
          <p class="verdict" [class.alert]="pre.verdict === 'LOW_GAIN'" [class.good]="pre.verdict === 'STRONG_GAIN' || pre.verdict === 'ALREADY_KNOWN'" role="status">{{ pre.message }}</p>
          @if (pre.labsCompared > 0) {
            <div class="gain-summary">
              <div class="duo">
                <div><span>Avant le cours</span><em>{{ pre.startCorrectPercent }} %</em><i [attr.aria-label]="pre.startCorrectPercent + ' % avant le cours'"><b class="before" [style.width.%]="pre.startCorrectPercent"></b></i></div>
                <div><span>Au quiz final (1re réponse)</span><em>{{ pre.endCorrectPercent }} %</em><i [attr.aria-label]="pre.endCorrectPercent + ' % au quiz final'"><b class="after" [style.width.%]="pre.endCorrectPercent"></b></i></div>
              </div>
              <ul class="counts">
                <li><strong>{{ pre.learned }}</strong> question{{ pre.learned > 1 ? 's' : '' }} apprise{{ pre.learned > 1 ? 's' : '' }} <small>(ratée ou inconnue avant, juste après)</small></li>
                <li><strong>{{ pre.lost }}</strong> perdue{{ pre.lost > 1 ? 's' : '' }} <small>(juste avant, plus juste après)</small></li>
                <li><strong>{{ pre.unknownAtStartPercent }} %</strong> de « Je ne sais pas » au départ</li>
              </ul>
            </div>
            <div class="gain-labs">
              <h3>Par laboratoire</h3>
              <ul>
                @for (lab of pre.labs; track lab.labCode) {
                  <li><a [routerLink]="['/labs', lab.labCode]">{{ lab.labCode }}</a><span>{{ lab.title }}</span><em>{{ lab.correctAtStart }}/{{ lab.total }} → {{ lab.correctAtEnd }}/{{ lab.total }}</em><b [class.neg]="lab.gainPoints < 0">{{ lab.gainPoints > 0 ? '+' : '' }}{{ lab.gainPoints }} pts</b></li>
                }
              </ul>
            </div>
          }
          <p class="reading">{{ pre.pretestsAnswered }} quiz de départ répondu{{ pre.pretestsAnswered > 1 ? 's' : '' }}, {{ pre.pretestsSkipped }} passé{{ pre.pretestsSkipped > 1 ? 's' : '' }}. Seuls les laboratoires terminés sont comparés, et la comparaison utilise la première réponse du quiz final.</p>
        </section>
      }
      <section class="role-lab">
        <div class="section-title"><div><p>Tuteur multi-rôle</p><h2>Choisis la posture utile</h2></div><span>Ollama local · aucun score modifié</span></div>
        <div class="roles" role="radiogroup" aria-label="Rôle du tuteur">
          @for (option of roles; track option.value) {
            <button type="button" [class.active]="role() === option.value" (click)="role.set(option.value)"><strong>{{ option.label }}</strong><small>{{ option.description }}</small></button>
          }
        </div>
        <div class="consult-form">
          <label>Laboratoire<input [value]="labCode()" (input)="labCode.set(asInput($event).value)" maxlength="40"></label>
          <label>Question<textarea [value]="question()" (input)="question.set(asInput($event).value)" maxlength="2000" rows="3"></textarea></label>
          <label>Contexte facultatif<textarea [value]="context()" (input)="context.set(asInput($event).value)" maxlength="6000" rows="4" placeholder="Colle ici ton raisonnement ou un extrait de code pertinent."></textarea></label>
          <button class="ask" type="button" (click)="consult()" [disabled]="consulting() || !question().trim()">{{ consulting() ? 'Réflexion locale…' : 'Consulter ce rôle' }}</button>
        </div>
        @if (tutorError()) { <p class="state error" role="alert">{{ tutorError() }}</p> }
        @if (tutorAnswer()) { <article class="answer"><small>{{ roleLabel(role()) }} · réponse locale</small><p>{{ tutorAnswer() }}</p></article> }
      </section>
    }
  `,
  styles: [`
    :host{display:block}.page-header{margin-bottom:1.4rem}.page-header p,.section-title p{color:var(--accent);font-size:.75rem;font-weight:800;letter-spacing:.09em;margin:0;text-transform:uppercase}.page-header h1{font-size:clamp(2rem,4vw,3rem);margin:.25rem 0}.page-header span,.eyebrow small,.section-title span,.metrics span,.metrics small,.disclaimer{color:var(--text-muted)}.recommendation,.analytics,.role-lab,.state{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:clamp(1rem,3vw,1.5rem)}.recommendation{box-shadow:inset 4px 0 var(--accent)}.eyebrow,.section-title{align-items:center;display:flex;justify-content:space-between}.eyebrow>span{background:var(--accent-soft);border-radius:99px;color:var(--link);font-size:.7rem;font-weight:800;padding:.35rem .65rem}.recommendation h2{font-size:1.4rem;margin:1rem 0 .35rem}.reason{color:var(--text);font-size:1.05rem;line-height:1.6}.recommendation article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.35rem;margin-top:.7rem;padding:.85rem}.recommendation article small{color:var(--accent);font-weight:750}.recommendation article strong{line-height:1.5}.recommendation details{border-top:1px solid var(--border);margin-top:1rem;padding-top:1rem}.recommendation summary{cursor:pointer;font-weight:750}.recommendation li,.analytics li{color:var(--text-muted);margin:.35rem 0}.actions{align-items:center;display:flex;flex-wrap:wrap;gap:.55rem;margin-top:1.1rem}.actions button,.actions a{background:var(--surface-raised);border:1px solid var(--border);border-radius:.55rem;color:var(--text);cursor:pointer;font:inherit;font-size:.78rem;font-weight:750;padding:.65rem .85rem;text-decoration:none}.actions button.primary{background:var(--accent);border-color:var(--accent);color:var(--on-accent)}.actions button.quiet{background:transparent;color:var(--text-muted)}.actions .accepted{background:var(--success-soft);border:1px solid var(--success);border-radius:.55rem;color:var(--success);font-size:.78rem;font-weight:800;padding:.65rem .85rem}.actions a{margin-left:auto}.actions button:disabled{cursor:wait;opacity:.55}.feedback{color:var(--success);font-size:.8rem;margin-bottom:0}.analytics,.role-lab{margin-top:1rem}.section-title h2{margin:.2rem 0}.metrics{display:grid;gap:.75rem;grid-template-columns:repeat(4,1fr);margin-top:1rem}.metrics article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.4rem;padding:.85rem}.metrics strong{font-size:1.55rem}.metrics i{background:var(--track);border-radius:99px;height:.35rem;overflow:hidden}.metrics b{background:linear-gradient(90deg,var(--accent),var(--success));display:block;height:100%}.disclaimer{border-top:1px solid var(--border);font-size:.72rem;margin:1rem 0 0;padding-top:.8rem}.roles{display:grid;gap:.6rem;grid-template-columns:repeat(5,1fr);margin-top:1rem}.roles button{background:var(--background);border:1px solid var(--border);border-radius:.7rem;color:var(--text);cursor:pointer;display:grid;gap:.35rem;padding:.8rem;text-align:left}.roles button.active{background:var(--accent-soft);border-color:var(--accent)}.roles small{color:var(--text-muted);font-size:.68rem}.consult-form{display:grid;gap:.8rem;margin-top:1rem}.consult-form label{color:var(--text-muted);display:grid;font-size:.76rem;font-weight:750;gap:.35rem}.consult-form input,.consult-form textarea{background:var(--input-background);border:1px solid var(--border);border-radius:.55rem;color:var(--text);font:inherit;padding:.7rem;resize:vertical}.ask{background:var(--accent);border:0;border-radius:.55rem;color:var(--on-accent);cursor:pointer;font:inherit;font-weight:800;justify-self:start;padding:.7rem 1rem}.ask:disabled{opacity:.5}.answer{background:var(--success-soft);border:1px solid var(--success);border-radius:.7rem;margin-top:1rem;padding:1rem}.answer small{color:var(--success);font-weight:800}.answer p{line-height:1.65;white-space:pre-wrap}.state.error{color:var(--danger);margin-top:1rem}.calibration{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);margin-top:1rem;padding:clamp(1rem,3vw,1.5rem)}.verdict{background:var(--background);border:1px solid var(--border);border-radius:.7rem;line-height:1.6;margin:1rem 0 0;padding:.85rem 1rem}.verdict.alert{border-color:var(--danger)}.verdict.good{border-color:var(--success)}.levels{display:grid;gap:.75rem;grid-template-columns:repeat(3,1fr);margin-top:1rem}.levels article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.5rem;padding:.85rem}.levels header{align-items:baseline;display:flex;justify-content:space-between}.levels header span,.levels small,.reading,.sources,.blind-spots p,.blind-spots small{color:var(--text-muted)}.levels small{font-size:.72rem}.stack{background:var(--track);border-radius:99px;display:flex;height:.7rem;overflow:hidden}.stack i{display:block;flex:0 1 0%}.stack .ok{background:var(--success)}.stack .part{background:var(--accent);opacity:.55}.stack .ko{background:var(--danger)}.reading{line-height:1.6;margin:1rem 0 .4rem}.sources{font-size:.78rem;list-style:none;margin:0;padding:0}.sources li{margin:.25rem 0}.gain-summary{display:grid;gap:1rem;grid-template-columns:2fr 1fr;margin-top:1rem}.gain-summary .duo{background:var(--background);border:1px solid var(--border);border-radius:.7rem;padding:.85rem}.gain-summary .duo span,.gain-summary .duo em,.counts small,.gain-labs span,.gain-labs em{color:var(--text-muted)}.counts{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.4rem;font-size:.8rem;list-style:none;margin:0;padding:.85rem}.counts strong{font-size:1.1rem}.gain-labs{border-top:1px solid var(--border);margin-top:1rem;padding-top:1rem}.gain-labs h3{margin:0}.gain-labs ul{display:grid;gap:.5rem;list-style:none;margin:.8rem 0 0;padding:0}.gain-labs li{align-items:center;background:var(--background);border:1px solid var(--border);border-radius:.6rem;display:grid;font-size:.8rem;gap:.75rem;grid-template-columns:auto 1fr auto auto;padding:.6rem .85rem}.gain-labs a{color:var(--link);font-weight:750;text-decoration:none}.gain-labs em{font-style:normal}.gain-labs b{color:var(--success)}.gain-labs b.neg{color:var(--danger)}@media(max-width:850px){.gain-summary{grid-template-columns:1fr}.gain-labs li{grid-template-columns:auto 1fr auto}.gain-labs li span{grid-column:1/4;order:5}}.trend{border-top:1px solid var(--border);margin-top:1rem;padding-top:1rem}.trend h3{margin:0}.weeks{display:grid;gap:.75rem;grid-template-columns:repeat(auto-fill,minmax(10.5rem,1fr));margin-top:1rem}.weeks article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.5rem;padding:.85rem}.weeks article.faint{opacity:.55}.weeks header{display:grid;gap:.1rem}.weeks header span,.weeks small{color:var(--text-muted);font-size:.72rem}.blind-spots{border-top:1px solid var(--border);margin-top:1rem;padding-top:1rem}.blind-spots h3{margin:0 0 .2rem}.blind-spots ul{display:grid;gap:.5rem;list-style:none;margin:.8rem 0 0;padding:0}.blind-spots li{align-items:center;background:var(--background);border:1px solid var(--border);border-radius:.6rem;display:grid;gap:.75rem;grid-template-columns:auto 1fr auto;padding:.7rem .85rem}.blind-spots li div{display:grid;gap:.2rem;line-height:1.45}.blind-spots .tag{background:var(--accent-soft);border-radius:99px;color:var(--text);font-size:.68rem;font-weight:800;padding:.25rem .55rem}.delays{display:grid;gap:.75rem;grid-template-columns:repeat(4,1fr);margin-top:1rem}.delays article{background:var(--background);border:1px solid var(--border);border-radius:.7rem;display:grid;gap:.5rem;padding:.85rem}.delays header span,.delays small,.delays .duo span,.delays .duo em,.retained{color:var(--text-muted)}.delays small{font-size:.72rem}.delays header{display:grid;gap:.1rem}.duo{display:grid;font-size:.7rem;gap:.55rem}.duo div{display:grid;gap:.2rem;grid-template-columns:1fr auto}.duo i{background:var(--track);border-radius:99px;grid-column:1/3;height:.55rem;overflow:hidden}.duo b{display:block;height:100%}.duo .before{background:var(--accent);opacity:.55}.duo .after{background:var(--success)}.duo em{font-style:normal;text-align:right}.retained{line-height:1.8;margin:1rem 0 0}.retained a{background:var(--accent-soft);border-radius:99px;color:var(--text);font-size:.72rem;font-weight:800;margin-left:.35rem;padding:.2rem .55rem;text-decoration:none}.blind-spots a{color:var(--link);font-size:.78rem;font-weight:750;text-decoration:none;white-space:nowrap}@media(max-width:1000px){.delays{grid-template-columns:repeat(2,1fr)}}@media(max-width:850px){.levels{grid-template-columns:1fr}}@media(max-width:600px){.delays{grid-template-columns:1fr}}@media(max-width:1000px){.roles{grid-template-columns:repeat(3,1fr)}}@media(max-width:850px){.metrics{grid-template-columns:1fr 1fr}}@media(max-width:600px){.eyebrow,.section-title{align-items:flex-start;flex-direction:column;gap:.55rem}.metrics,.roles{grid-template-columns:1fr}.actions a{margin-left:0;width:100%}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CoachComponent {
  private readonly api = inject(AdaptationApiService);
  private readonly tutor = inject(TutorApiService);
  readonly loading = signal(true);
  readonly deciding = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly recommendation = signal<AdaptationRecommendation | null>(null);
  readonly insights = signal<AdaptationInsights | null>(null);
  readonly calibration = signal<Calibration | null>(null);
  readonly retention = signal<Retention | null>(null);
  readonly pretestGain = signal<PretestGainSummary | null>(null);
  readonly role = signal<TutorRole>('TEACHER');
  readonly labCode = signal('JAVA-01');
  readonly question = signal('Comment puis-je mieux comprendre ce laboratoire ?');
  readonly context = signal('');
  readonly consulting = signal(false);
  readonly tutorAnswer = signal('');
  readonly tutorError = signal('');
  readonly roles: { value: TutorRole; label: string; description: string }[] = [
    { value: 'TEACHER', label: 'Professeur', description: 'Explique et vérifie' },
    { value: 'COACH', label: 'Coach', description: 'Débloque la prochaine action' },
    { value: 'REVIEWER', label: 'Reviewer', description: 'Relit avec exigence' },
    { value: 'CLIENT', label: 'Client', description: 'Clarifie le besoin' },
    { value: 'TECH_LEAD', label: 'Tech lead', description: 'Challenge les compromis' }
  ];

  constructor() {
    forkJoin({ recommendation: this.api.recommendation(), insights: this.api.insights(), calibration: this.api.calibration(), retention: this.api.retention(), pretestGain: this.api.pretestGain() }).subscribe({
      next: ({ recommendation, insights, calibration, retention, pretestGain }) => {
        this.recommendation.set(recommendation);
        this.insights.set(insights);
        this.calibration.set(calibration);
        this.retention.set(retention);
        this.pretestGain.set(pretestGain);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Le coach local est indisponible. Vérifie que l’API DLR est démarrée.');
        this.loading.set(false);
      }
    });
  }

  decide(decision: AdaptationDecision): void {
    const item = this.recommendation();
    if (!item || this.deciding()) return;
    this.deciding.set(true);
    this.feedback.set('');
    this.api.decide(item.id, decision).subscribe({
      next: (recommendation) => {
        this.recommendation.set(recommendation);
        this.feedback.set(({ ACCEPT: 'Proposition acceptée.', POSTPONE: 'Proposition reportée de trois jours.', REPLACE: 'Nouvelle proposition générée.', IGNORE: 'Proposition ignorée.' })[decision]);
        this.deciding.set(false);
      },
      error: () => {
        this.feedback.set('Cette proposition a déjà été traitée ou l’API est indisponible. L’état a été rechargé.');
        this.api.recommendation().subscribe({ next: (recommendation) => this.recommendation.set(recommendation) });
        this.deciding.set(false);
      }
    });
  }

  difficultyLabel(difficulty: AdaptationRecommendation['difficulty']): string {
    return ({ GUIDE: 'Découverte guidée', RENFORCEMENT: 'Renforcement', TRANSFERT: 'Mise en transfert', CHALLENGE: 'Défi autonome' })[difficulty];
  }

  gapLabel(gap: number): string {
    if (gap === 0) return 'Confiance et réussite alignées';
    return gap > 0 ? `+${gap} points : plus sûr·e que réussi·e` : `${gap} points : plus de réussite que de confiance`;
  }

  confidenceLabel(level: number): string {
    return ({ 1: 'Pas sûr·e', 2: 'Plutôt sûr·e', 3: 'Sûr·e' } as Record<number, string>)[level] ?? String(level);
  }

  consult(): void {
    if (this.consulting() || !this.question().trim()) return;
    this.consulting.set(true);
    this.tutorAnswer.set('');
    this.tutorError.set('');
    this.tutor.consult(this.role(), this.labCode().trim(), this.context(), this.question()).subscribe({
      next: (answer) => { this.tutorAnswer.set(answer.content); this.consulting.set(false); },
      error: () => { this.tutorError.set('Ollama est indisponible. Le parcours et les scores restent pleinement utilisables.'); this.consulting.set(false); }
    });
  }

  roleLabel(role: TutorRole): string {
    return this.roles.find((option) => option.value === role)?.label ?? role;
  }

  asInput(event: Event): HTMLInputElement | HTMLTextAreaElement {
    return event.target as HTMLInputElement | HTMLTextAreaElement;
  }
}
